package top.emilejones.hhu.preprocessing.structure.v2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.CatalogTitleLevelCorrectorPlus;
import top.emilejones.hhu.preprocessing.handler.structure.EmptyTitleLineRemover;
import top.emilejones.hhu.preprocessing.structure.enums.TitleType;
import top.emilejones.hhu.preprocessing.structure.tree.Node;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 基于 {@link AbstractCorrectableTitleTreeExtractor} 实现的可修正标题树提取器。
 * 通过 Markdown 语法（# 数量）识别标题层级，并在结构提取后借助 LLM 修正树结构。
 *
 * @author EmileJones
 */
public class CorrectableTitleTreeExtractor extends AbstractCorrectableTitleTreeExtractor {

    private String[] lines;
    private final ModelClient modelClient;
    private final Executor executor;
    private final Gson gson = new Gson();

    public CorrectableTitleTreeExtractor(ModelClient modelClient, Executor executor) {
        this.modelClient = modelClient;
        this.executor = executor;
    }

    @Override
    protected String initOriginText(String originText) {
        MarkdownFileHandler catalogCorrector = new CatalogTitleLevelCorrectorPlus();
        MarkdownFileHandler emptyTitleLineRemover = new EmptyTitleLineRemover();
        String withoutCatalog = catalogCorrector.handle(originText);
        String catalogPattern = "(?m)^##\\s*目\\s*录.*\\R.*\\R?";
        String withoutCatalogBlock = withoutCatalog.replaceAll(catalogPattern, "");
        return emptyTitleLineRemover.handle(withoutCatalogBlock);
    }

    private int getHeadingLevel(String line) {
        String trimmed = line.trim();
        int count = 0;
        for (int i = 0; i < trimmed.length() && i < 6; i++) {
            if (trimmed.charAt(i) == '#') {
                count++;
            } else {
                break;
            }
        }
        if (count > 0 && count < trimmed.length() && Character.isWhitespace(trimmed.charAt(count))) {
            return count;
        }
        return 0;
    }

    @Override
    protected Node extractStructureTree(String originText) {
        this.lines = originText.split("\\R");
        Node root = new Node(-1, TitleType.NilType);
        root.setLevel(0);

        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);

        for (int i = 0; i < lines.length; i++) {
            int level = getHeadingLevel(lines[i]);
            if (level <= 0) {
                continue;
            }

            Node node = new Node(i, TitleType.TYPE_ARABIC_NUMBER);
            node.setLevel(level);

            while (stack.size() > 1 && stack.peek().getLevel() >= level) {
                stack.pop();
            }

            Node parent = stack.peek();
            parent.appendChild(node);
            node.setParent(parent);
            stack.push(node);
        }

        return root;
    }

    @Override
    protected void correctorStructureTree(Node root) {
        correctStructureGlobally(root);
//        correctStructureBottomUp(root);
    }

    /**
     * 全局结构修正：将所有标题节点一次性输入 LLM，获取整体层级修正后重建树。
     */
    private void correctStructureGlobally(Node root) {
        List<Node> allNodes = new ArrayList<>();
        collectTitleNodes(root, allNodes);

        if (allNodes.isEmpty()) {
            return;
        }

        allNodes.sort(Comparator.comparingInt(Node::getIndex));

        String nodesContext = allNodes.stream()
                .map(node -> {
                    String text = (node.getIndex() >= 0 && node.getIndex() < lines.length)
                            ? lines[node.getIndex()].trim()
                            : "";
                    return String.format("(index:%d, level:%d, text:\"%s\")", node.getIndex(), node.getLevel(), truncate(text));
                })
                .collect(java.util.stream.Collectors.joining("\n"));

        String systemPrompt = """
                你是一个文档结构分析专家。你的任务是接收包含行号、当前层级和标题文本的 Markdown 标题列表，通过分析语义和上下文逻辑，输出严格修正后的层级结构（level）。

                【核心规则】
                1. 层级规范：level 从 1 开始（一级标题对应一个 #）。标题层级递进应保持逻辑连续，避免无故跨级。
                2. 判定逻辑：
                   - 若当前标题是前置标题的子概念或具体步骤，其 level 需相应增加（+1）。
                   - 语义深度、逻辑层级平行或同属一个序号体系的标题，level 必须保持一致。
                   - 重点关注非叶子节点（即包含子标题的节点）的结构合理性。
                3. 边界约束：绝对保持输入标题的原始顺序，不得增减或修改 index。
                4. 输出格式：仅输出原生的 JSON 数组格式。严禁输出任何解释性文字、前言或 ```json 等 Markdown 格式控制符。

                【输入输出示例】
                输入：
                (index:0, level:1, text:"# 项目背景")
                (index:10, level:3, text:"### 市场现状")
                (index:15, level:3, text:"### 数据采集机制")
                (index:20, level:1, text:"# 核心架构")
                输出：
                [{"index":0, "level":1}, {"index":10, "level":2}, {"index":15, "level":3}, {"index":20, "level":1}]
                """;

        String userPrompt = "输入:\n" + nodesContext + "\n输出:\n";

        String llmResponse = modelClient.llm(systemPrompt, userPrompt);
        List<Map<String, Object>> correctedData = parseJson(llmResponse);
        rebuildTree(root, allNodes, correctedData);
    }

    /**
     * 自底向上逐层并发修正：按树深度自底向上分层，每层内并发调用 LLM 判断各节点的子节点是否合规，
     * 该层全部完成后统一 rebuildTree，再继续上一层。
     */
    private void correctStructureBottomUp(Node root) {
        int maxDepth = calculateMaxDepth(root);

        for (int depth = maxDepth - 1; depth >= 0; depth--) {
            Map<Integer, List<Node>> depthToNodes = new HashMap<>();
            collectNodesByDepth(root, depthToNodes, 0);

            List<Node> nonLeafNodes = depthToNodes.getOrDefault(depth, Collections.emptyList())
                    .stream()
                    .filter(node -> node.childrenNumber() > 0)
                    .toList();

            if (nonLeafNodes.isEmpty()) {
                continue;
            }

            List<CompletableFuture<Map<Integer, Integer>>> futures = nonLeafNodes.stream()
                    .map(node -> CompletableFuture.supplyAsync(() -> correctChildren(node), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            Map<Integer, Integer> corrections = new HashMap<>();
            for (CompletableFuture<Map<Integer, Integer>> future : futures) {
                corrections.putAll(future.getNow(Collections.emptyMap()));
            }

            if (!corrections.isEmpty()) {
                List<Node> allNodes = new ArrayList<>();
                collectTitleNodes(root, allNodes);
                rebuildTree(root, allNodes, corrections);
            }
        }
    }

    /**
     * 对单个父节点调用 LLM，判断其直接子节点的层级是否合规。
     *
     * @return 需要修正的 (index -> level) 映射，若全部合规则返回空 Map
     */
    private Map<Integer, Integer> correctChildren(Node parent) {
        StringBuilder promptBuilder = new StringBuilder();
        String parentText = (parent.getIndex() >= 0 && parent.getIndex() < lines.length)
                ? lines[parent.getIndex()].trim()
                : "";

        promptBuilder.append(String.format("父节点: (index:%d, level:%d, text:\"%s\")%n",
                parent.getIndex(), parent.getLevel(), truncate(parentText)));
        promptBuilder.append("子节点:\n");

        for (int i = 0; i < parent.childrenNumber(); i++) {
            Node child = parent.getChild(i);
            String childText = (child.getIndex() >= 0 && child.getIndex() < lines.length)
                    ? lines[child.getIndex()].trim()
                    : "";
            promptBuilder.append(String.format("- (index:%d, level:%d, text:\"%s\")%n",
                    child.getIndex(), child.getLevel(), truncate(childText)));
        }

        String systemPrompt = """
                你是一个文档结构验证专家。请判断给定父节点的子节点层级是否合规。
                合规标准：子节点的 level 应等于父节点 level + 1；若存在并列关系可保持同级，但不得无故跨级（如父节点 level 2 的子节点直接跳到 level 4）。
                如果全部合规，输出空数组 []。
                如果有不合规的，输出需要修正的子节点 JSON 数组：[{"index": x, "level": y}, ...]
                仅输出 JSON，不要解释。

                【示例】
                父节点: (index:10, level:2, text:"## 市场现状")
                子节点:
                - (index:12, level:4, text:"#### 国内市场")
                - (index:15, level:3, text:"### 国外市场")

                输出：
                [{"index":12, "level":3}]
                """;

        String userPrompt = promptBuilder.toString() + "\n输出:\n";

        try {
            String llmResponse = modelClient.llm(systemPrompt, userPrompt);
            List<Map<String, Object>> correctedData = parseJson(llmResponse);
            Map<Integer, Integer> result = new HashMap<>();
            for (Map<String, Object> item : correctedData) {
                Object indexObj = item.get("index");
                Object levelObj = item.get("level");
                if (indexObj != null && levelObj != null) {
                    int index = (int) Double.parseDouble(indexObj.toString());
                    int level = (int) Double.parseDouble(levelObj.toString());
                    result.put(index, level);
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void collectTitleNodes(Node node, List<Node> list) {
        if (node.getTitleType() != TitleType.NilType) {
            list.add(node);
        }
        for (int i = 0; i < node.childrenNumber(); i++) {
            collectTitleNodes(node.getChild(i), list);
        }
    }

    private void collectNodesByDepth(Node node, Map<Integer, List<Node>> depthToNodes, int depth) {
        if (node.getTitleType() != TitleType.NilType) {
            depthToNodes.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);
        }
        for (int i = 0; i < node.childrenNumber(); i++) {
            collectNodesByDepth(node.getChild(i), depthToNodes, depth + 1);
        }
    }

    private int calculateMaxDepth(Node node) {
        if (node.childrenNumber() == 0) {
            return 0;
        }
        int max = 0;
        for (int i = 0; i < node.childrenNumber(); i++) {
            max = Math.max(max, calculateMaxDepth(node.getChild(i)));
        }
        return max + 1;
    }

    private String truncate(String text) {
        if (text.length() > 10) {
            return text.substring(0, 10) + "...";
        }
        return text;
    }

    private List<Map<String, Object>> parseJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        response = response.trim();

        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(response, type);
    }

    private void rebuildTree(Node root, List<Node> originalNodes, List<Map<String, Object>> correctedData) {
        Map<Integer, Integer> indexToLevel = new HashMap<>();
        for (Map<String, Object> item : correctedData) {
            Object indexObj = item.get("index");
            Object levelObj = item.get("level");
            if (indexObj != null && levelObj != null) {
                int index = (int) Double.parseDouble(indexObj.toString());
                int level = (int) Double.parseDouble(levelObj.toString());
                indexToLevel.put(index, level);
            }
        }
        rebuildTree(root, originalNodes, indexToLevel);
    }

    private void rebuildTree(Node root, List<Node> originalNodes, Map<Integer, Integer> indexToLevel) {
        Node newRoot = new Node(-1, TitleType.NilType);
        newRoot.setLevel(0);

        List<Node> sortedNodes = originalNodes.stream()
                .sorted(Comparator.comparingInt(Node::getIndex))
                .toList();

        Node lastNode = newRoot;
        for (Node originalNode : sortedNodes) {
            Integer correctedLevel = indexToLevel.get(originalNode.getIndex());
            if (correctedLevel == null) {
                correctedLevel = originalNode.getLevel();
            }

            Node newNode = new Node(originalNode.getIndex(), originalNode.getTitleType());
            newNode.setLevel(correctedLevel);

            Node potentialParent = lastNode;
            while (potentialParent != null && potentialParent.getLevel() >= newNode.getLevel()) {
                potentialParent = potentialParent.getParent();
            }

            if (potentialParent != null) {
                potentialParent.appendChild(newNode);
                newNode.setParent(potentialParent);
            } else {
                newRoot.appendChild(newNode);
                newNode.setParent(newRoot);
            }
            lastNode = newNode;
        }

        clearNodeChildren(root);
        for (int i = 0; i < newRoot.childrenNumber(); i++) {
            Node child = newRoot.getChild(i);
            root.appendChild(child);
            child.setParent(root);
        }
    }

    private void clearNodeChildren(Node node) {
        try {
            java.lang.reflect.Field field = Node.class.getDeclaredField("children");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Node> children = (List<Node>) field.get(node);
            children.clear();
        } catch (Exception e) {
            throw new RuntimeException("无法清空节点子节点", e);
        }
    }

    @Override
    protected String correctOriginTextByStructureTree(Node root) {
        Map<Integer, Integer> indexToLevel = new HashMap<>();
        collectIndexToLevel(root, indexToLevel);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (indexToLevel.containsKey(i)) {
                int level = indexToLevel.get(i);
                String prefix = String.join("", Collections.nCopies(level, "#"));
                String content = stripHeadingMarkers(lines[i].trim());
                sb.append(prefix).append(" ").append(content).append("\n");
            } else {
                sb.append(lines[i]).append("\n");
            }
        }
        return sb.toString();
    }

    private void collectIndexToLevel(Node node, Map<Integer, Integer> map) {
        if (node.getTitleType() != TitleType.NilType) {
            map.put(node.getIndex(), node.getLevel());
        }
        for (int i = 0; i < node.childrenNumber(); i++) {
            collectIndexToLevel(node.getChild(i), map);
        }
    }

    private String stripHeadingMarkers(String line) {
        String trimmed = line.trim();
        int i = 0;
        while (i < trimmed.length() && i < 6 && trimmed.charAt(i) == '#') {
            i++;
        }
        if (i > 0 && i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i))) {
            return trimmed.substring(i).trim();
        }
        return trimmed;
    }
}
