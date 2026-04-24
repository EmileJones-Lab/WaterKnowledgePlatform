package top.emilejones.hhu.preprocessing.structure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.preprocessing.structure.enums.TitleType;
import top.emilejones.hhu.preprocessing.structure.tree.Node;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * TitleTreeExtractorWithAI 是一个装饰器，用于在现有的标题提取器基础上，
 * 利用 AI 模型对提取出的标题结构树进行语义纠正。
 *
 * @author emilejones
 */
public class TitleTreeExtractorWithAI extends AbstractTitleTreeExtractor {

    private final AbstractTitleTreeExtractor delegate;
    private final ModelClient modelClient;
    private final ExecutorService llmExecutor;
    private final Gson gson = new Gson();

    public TitleTreeExtractorWithAI(AbstractTitleTreeExtractor delegate, ModelClient modelClient, ExecutorService llmExecutor) {
        this.delegate = delegate;
        this.modelClient = modelClient;
        this.llmExecutor = llmExecutor;
    }

    @Override
    protected String initOriginText(String originText) {
        String processedText = delegate.initOriginText(originText);
        String[] lines = processedText.split("\\R");

        String systemPrompt = "Role: 你是一个严谨的OCR文本修复与数据清洗专家。\n" +
                "Task: 修复OCR提取的文本。\n" +
                "Rules:\n" +
                "1. 拆分：若文本包含多个独立编号（如(1)...(2)...）或标题粘连，按逻辑拆分。\n" +
                "2. 格式：在层级符号/编号（如 2.3, (1), （3））与正文之间，强制保留一个空格。\n" +
                "3. 输出：仅输出严格的JSON。若无需修复，返回{\"rewrite\":false,\"chunks\":[]}；若需修复，返回{\"rewrite\":true,\"chunks\":[\"修复片段1\",\"修复片段2\"]}。\n" +
                "\n" +
                "Examples:\n" +
                "Input: 2.3.2.2整河段河道流量演算（1）先合后演\n" +
                "Output: {\"rewrite\": true, \"chunks\": [\"2.3.2.2 整河段河道流量演算\", \"（1） 先合后演\"]}\n" +
                "\n" +
                "Input: 2.3.2.1 选用资料\n" +
                "Output: {\"rewrite\": false, \"chunks\": []}\n" +
                "\n" +
                "Input: 2.4.5鲁台子与淮南洪峰水位相关（二）\n" +
                "Output: {\"rewrite\": true, \"chunks\": [\"2.4.5 鲁台子与淮南洪峰水位相关（二）\"]}\n" +
                "\n" +
                "Input: (1)以蚌埠站同时水位为参数的预报方案；(2)计算区间前期影响雨量，选用合适单位线进行汇流计算；（3）采用马斯京根法进行河道流量演算。\n" +
                "Output: {\"rewrite\": true, \"chunks\": [\"(1) 以蚌埠站同时水位为参数的预报方案；\", \"(2) 计算区间前期影响雨量，选用合适单位线进行汇流计算；\", \"（3） 采用马斯京根法进行河道流量演算。\"]}";

        List<CompletableFuture<IndexedLines>> futures = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            final int index = i;
            if (isTitle(line)) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    String userPrompt = "Input: " + line + "\nOutput:";
                    try {
                        String llmResponse = modelClient.llm(systemPrompt, userPrompt);
                        String jsonPart = extractJson(llmResponse);
                        LlmCorrection correction = gson.fromJson(jsonPart, LlmCorrection.class);
                        if (correction != null && correction.rewrite && correction.chunks != null && !correction.chunks.isEmpty()) {
                            return new IndexedLines(index, correction.chunks);
                        } else {
                            return new IndexedLines(index, Collections.singletonList(line));
                        }
                    } catch (Exception e) {
                        return new IndexedLines(index, Collections.singletonList(line));
                    }
                }, llmExecutor));
            } else {
                futures.add(CompletableFuture.completedFuture(new IndexedLines(index, Collections.singletonList(line))));
            }
        }

        List<String> resultLines = futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(il -> il.index))
                .flatMap(il -> il.lines.stream())
                .collect(Collectors.toList());

        return String.join("\n", resultLines);
    }

    @Override
    protected boolean isTitle(String line) {
        return delegate.isTitle(line);
    }

    @Override
    protected boolean isFirstTitle(String line) {
        return delegate.isFirstTitle(line);
    }

    @Override
    protected String correctOriginTextByStructureTree(Node root) {
        return delegate.correctOriginTextByStructureTree(root);
    }

    @Override
    protected Node extractStructureTree(String processedText) {
        // 1. 调用被装饰者的 extractStructureTree 获取初步构建的树
        Node initialRoot = delegate.extractStructureTree(processedText);
        
        // 获取处理后的文本行，用于提取标题文本供 AI 参考
        String[] lines = processedText.split("\\R");

        // 2. 收集所有节点并按索引排序，准备 AI 上下文
        List<Node> allNodes = new ArrayList<>();
        collectAllNodes(initialRoot, allNodes);
        allNodes.sort(Comparator.comparingInt(Node::getIndex));

        if (allNodes.isEmpty()) {
            return initialRoot;
        }

        String nodesContext = allNodes.stream()
                .map(node -> {
                    String text = (node.getIndex() >= 0 && node.getIndex() < lines.length) ? lines[node.getIndex()].trim() : "";
                    return String.format("(index:%d, level:%d, text:\"%s\")", node.getIndex(), node.getLevel(), text);
                })
                .collect(Collectors.joining("\n"));

        // 3. 准备 System prompt 和 User prompt
        String systemPrompt = """
                你是一个文档结构分析专家。你的任务是接收包含行号、初始层级和标题文本的 Markdown 标题列表，通过分析语义和上下文逻辑，输出严格修正后的层级结构（level）。
                
                【核心规则】
                1. 层级规范：level 从 1 开始（一级标题）。标题层级递进应保持逻辑连续，避免无故跨级。
                2. 判定逻辑：
                   - 显性序号深度映射（最高优先级）：当标题包含标准多级编号（如 "2.3.2", "2.3.2.1"）时，其相对 level 必须严格遵循编号的层级深度。例如，若 "2.3.2" 被定义为 level 1，则 "2.3.2.1" 必须严格向下递进一层为 level 2。
                   - 异构序号平级兼容：在同一个父标题下，不同体系的子序号（如正文列表项 "（1）" 与结构子标题 "2.3.2.1"）如果均直接从属于该父标题，应当分配相同的 level，编号深度的规则不受中间穿插的其他类型序号干扰。
                   - 隐性语义：若无序号，基于上下文判断从属关系。若当前标题代表前置标题的子概念或具体步骤，其 level 需相应增加（+1）。
                   - 并列关系：语义深度、逻辑层级平行或同属一个序号体系的标题，level 必须保持一致。
                3. 边界约束：绝对保持输入标题的原始顺序，不得增减或修改 index。
                4. 输出格式：仅输出原生的 JSON 数组格式。严禁输出任何解释性文字、前言或 ```json 等 Markdown 格式控制符。
                
                【输入输出示例】
                
                示例 1：语义与基础序号推导
                输入：
                (index:0, level:1, text:"一、项目背景")
                (index:10, level:1, text:"1.1 市场现状")
                (index:15, level:1, text:"数据采集机制")
                (index:20, level:1, text:"二、核心架构")
                输出：
                [{"index":0, "level":1}, {"index":10, "level":2}, {"index":15, "level":3}, {"index":20, "level":1}]
                
                示例 2：多套异构序号的深度映射
                输入：
                (index:0, level:1, text:"2.3.2 河道流量演算")
                (index:11, level:2, text:"（1） 先合后演。")\s
                (index:13, level:2, text:"（2）先演后合。")\s
                (index:16, level:3, text:"2.3.2.1 选用资料")\s
                (index:20, level:3, text:"2.3.2.2 整河段河道流量演算")
                输出：
                [{"index":0, "level":1}, {"index":11, "level":2}, {"index":13, "level":2}, {"index":16, "level":2}, {"index":20, "level":2}]
                """;

        String userPrompt = "输入:\n" + nodesContext + "\n输出:\n";


        // 调用 LLM
        String llmResponse = modelClient.llm(systemPrompt, userPrompt);

        // 4. 解析并验证 LLM 输出
        List<Map<String, Object>> correctedData;
        try {
            String jsonPart = extractJson(llmResponse);
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            correctedData = gson.fromJson(jsonPart, type);
        } catch (Exception e) {
            throw new RuntimeException("LLM 输出格式错误，无法解析为 JSON: " + llmResponse, e);
        }

        // 5. 根据输出的 JSON 构造新的树结构
        return rebuildTree(allNodes, correctedData);
    }

    private void collectAllNodes(Node node, List<Node> list) {
        if (node.getTitleType() != TitleType.NilType) {
            list.add(node);
        }
        for (int i = 0; i < node.childrenNumber(); i++) {
            collectAllNodes(node.getChild(i), list);
        }
    }

    private String extractJson(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }

    private Node rebuildTree(List<Node> allNodes, List<Map<String, Object>> correctedData) {
        Map<Integer, Integer> indexToLevel = new HashMap<>();
        for (Map<String, Object> item : correctedData) {
            Object indexObj = item.get("index");
            Object levelObj = item.get("level");
            if (indexObj != null && levelObj != null) {
                // Gson 解析数字可能为 Double，统一转字符串再解析
                int index = (int) Double.parseDouble(indexObj.toString());
                int level = (int) Double.parseDouble(levelObj.toString());
                indexToLevel.put(index, level);
            }
        }

        Node newRoot = new Node(-1, TitleType.NilType);
        newRoot.setLevel(0);
        
        List<Node> sortedNodes = allNodes.stream()
                .sorted(Comparator.comparingInt(Node::getIndex))
                .collect(Collectors.toList());

        Node lastNode = newRoot;
        for (Node node : sortedNodes) {
            Integer correctedLevel = indexToLevel.get(node.getIndex());
            if (correctedLevel == null) {
                correctedLevel = node.getLevel();
            }
            
            Node newNode = new Node(node.getIndex(), node.getTitleType());
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

        return newRoot;
    }

    private static class IndexedLines {
        final int index;
        final List<String> lines;

        IndexedLines(int index, List<String> lines) {
            this.index = index;
            this.lines = lines;
        }
    }

    private static class LlmCorrection {
        boolean rewrite;
        List<String> chunks;
    }
}
