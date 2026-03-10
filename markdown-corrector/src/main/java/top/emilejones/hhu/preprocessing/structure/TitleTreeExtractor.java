package top.emilejones.hhu.preprocessing.structure;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;
import top.emilejones.hhu.preprocessing.handler.structure.CatalogTitleLevelCorrectorPlus;
import top.emilejones.hhu.preprocessing.structure.enums.TitleType;
import top.emilejones.hhu.preprocessing.structure.tree.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实现通过扫描标题构建结构树，并以此规范Markdown文档标题结构。
 *
 * @author EmileJones
 * @author yeyezhi
 */
public class TitleTreeExtractor extends AbstractTitleTreeExtractor {

    private String[] lines;

    @Override
    protected String initOriginText(String originText) {
        String haveNoCatalogText = removeCatalog(originText);

        String[] textLines = haveNoCatalogText.split("\\R");
        StringBuilder result = new StringBuilder();
        for (String line : textLines) {
            String workingLine = line;
            String trimmedLine = workingLine.trim();
            if (trimmedLine.isEmpty()) {
                result.append(line).append(System.lineSeparator());
                continue;
            }

            boolean lineChanged = false;
            int firstNonSpaceIndex = 0;
            while (firstNonSpaceIndex < workingLine.length() && Character.isWhitespace(workingLine.charAt(firstNonSpaceIndex))) {
                firstNonSpaceIndex++;
            }
            if (firstNonSpaceIndex < workingLine.length() && workingLine.charAt(firstNonSpaceIndex) == '#') {
                workingLine = workingLine.substring(0, firstNonSpaceIndex) + workingLine.substring(firstNonSpaceIndex + 1);
                trimmedLine = workingLine.trim();
                lineChanged = true;
            }

            String processedLine = trimmedLine;

            // 处理层级标题：删除数字和点号之间的空格（如 "1. 1. 1" -> "1.1.1"）
            // 但保留数字序列和文字之间的空格
            // 匹配格式：可选的#号 + 数字序列（如 "1. 2. 3" 或 "1. 2. 3. 4"）+ 空格 + 文字
            Pattern hierarchicalPattern = Pattern.compile("^((?:[0-9]+\\s*\\.\\s*){1,3}[0-9]+)(\\s+)(.+)$");
            Matcher hierarchicalMatcher = hierarchicalPattern.matcher(trimmedLine);
            if (hierarchicalMatcher.matches()) {
                String numberSequence = hierarchicalMatcher.group(1);
                String spaceAfterNumbers = hierarchicalMatcher.group(2);
                String textAfter = hierarchicalMatcher.group(3);

                // 删除数字和点号之间的空格，但保留点号
                // 将 "1. 2. 3" 转换为 "1.2.3"
                String normalizedSequence = numberSequence.replaceAll("(\\d+)\\s*\\.\\s*", "$1.");

                processedLine = normalizedSequence + spaceAfterNumbers + textAfter;
                lineChanged = true;
            }

            for (TitleType type : TitleType.values()) {
                if (type == TitleType.NilType || type.getTitleRegex() == null) {
                    continue;
                }

                String patternString = type.getTitleRegex().replace("[\\s].*", "");
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(processedLine);

                if (matcher.lookingAt()) {
                    int matchEnd = matcher.end();
                    if (processedLine.length() > matchEnd && !Character.isWhitespace(processedLine.charAt(matchEnd))) {
                        processedLine = processedLine.substring(0, matchEnd)
                                + " "
                                + processedLine.substring(matchEnd);
                        lineChanged = true;
                        break;
                    }
                }
            }

            if (lineChanged) {
                // 保留原始行的前导空格
                String leadingSpaces = line.substring(0, firstNonSpaceIndex);
                result.append(leadingSpaces).append(processedLine).append(System.lineSeparator());
            } else {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    @Override
    protected boolean isTitle(String line) {
        return getTitleType(line) != null;
    }

    private TitleType getTitleType(String line) {
        for (TitleType type : TitleType.values()) {
            if (type.getTitleRegex() != null && line.matches(type.getTitleRegex())) {
                return type;
            }
        }
        return null;
    }

    @Override
    protected boolean isFirstTitle(String line) {
        for (TitleType type : TitleType.values()) {
            if (type.getFirstTitleRegex() != null && line.matches(type.getFirstTitleRegex())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Node extractStructureTree(String originText) {
        this.lines = originText.split("\\R");
        Node root = new Node(-1, TitleType.NilType);
        Node lastNode = root;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!isTitle(line)) {
                continue;
            }

            TitleType type = Objects.requireNonNull(getTitleType(line));
            Node currentNode = new Node(i, type);

            if (lastNode.getTitleType() == TitleType.NilType) {
                root.appendChild(currentNode);
                currentNode.setParent(root);
            } else if (isFirstTitle(line)) {
                lastNode.appendChild(currentNode);
                currentNode.setParent(lastNode);
            } else {
                Node parentFinder = lastNode;
                while (parentFinder.getTitleType() != TitleType.NilType) {
                    if (parentFinder.getTitleType() == currentNode.getTitleType()) {
                        break;
                    }
                    parentFinder = parentFinder.getParent();
                }

                Node parent = parentFinder.getParent();
                if (parent != null) {
                    parent.appendChild(currentNode);
                    currentNode.setParent(parent);
                } else {
                    root.appendChild(currentNode);
                    currentNode.setParent(root);
                }
            }
            lastNode = currentNode;
        }
        return root;
    }

    @Override
    protected String correctOriginTextByStructureTree(Node root) {
        Map<Integer, Node> lineToNode = new HashMap<>();
        mapLinesToNodes(root, lineToNode);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.lines.length; i++) {
            if (i == 0) {
                String docName = this.lines[i].trim().replaceFirst("^#+\\s*", "");
                sb.append("# ").append(docName).append("\n");
                continue;
            }

            if (lineToNode.containsKey(i)) {
                Node node = lineToNode.get(i);
                int depth = getDepth(node);
                String prefix = String.join("", Collections.nCopies(depth + 1, "#"));

                String originalTitleLine = this.lines[i].trim();
                sb.append(prefix).append(" ").append(originalTitleLine).append("\n");
            } else {
                sb.append(this.lines[i]).append("\n");
            }
        }
        return sb.toString();
    }

    private void mapLinesToNodes(Node node, Map<Integer, Node> map) {
        if (node.getTitleType() != TitleType.NilType) {
            map.put(node.getIndex(), node);
        }
        for (int i = 0; i < node.childrenNumber(); i++) {
            mapLinesToNodes(node.getChild(i), map);
        }
    }

    private int getDepth(Node node) {
        int depth = 0;
        Node current = node;
        while (current != null && current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    /**
     * 在控制台打印标题层次结构树，用于直观查看提取效果
     *
     * @param root 标题树的根节点
     */
    public void printStructureTree(Node root) {
        if (root == null) {
            System.out.println("标题树为空");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========== 标题层次结构树 ==========\n");
        printStructureTreeRecursive(root, "", true, sb);
        sb.append("===================================\n");
        System.out.println(sb.toString());
    }

    /**
     * 递归打印标题树结构
     *
     * @param node   当前节点
     * @param indent 当前缩进字符串
     * @param isLast 是否是最后一个子节点
     * @param sb     用于构建输出的StringBuilder
     */
    private void printStructureTreeRecursive(Node node, String indent, boolean isLast, StringBuilder sb) {
        if (node == null) {
            return;
        }

        // 打印当前节点（跳过根节点NilType）
        if (node.getTitleType() != TitleType.NilType) {
            String prefix = isLast ? "└── " : "├── ";
            sb.append(indent).append(prefix);

            // 获取标题文本
            String titleText = "";
            if (node.getIndex() >= 0 && node.getIndex() < lines.length) {
                titleText = lines[node.getIndex()].trim();
            }

            // 打印节点信息：行号、标题类型、标题内容
            sb.append(String.format("[行%d] [%s] %s\n",
                    node.getIndex() + 1,
                    node.getTitleType().name(),
                    titleText));
        }

        // 处理子节点
        int childCount = node.childrenNumber();
        for (int i = 0; i < childCount; i++) {
            Node child = node.getChild(i);
            boolean isLastChild = (i == childCount - 1);
            String childIndent = indent + (isLast ? "    " : "│   ");
            printStructureTreeRecursive(child, childIndent, isLastChild, sb);
        }
    }

    private String removeCatalog(String str) {
        // 预处理后，目录标题会变为 "## 目录"，且目录正文会被合并到紧接着的下一行中
        MarkdownFileHandler handler = new CatalogTitleLevelCorrectorPlus();

        // 1. 获取处理后的 Markdown 文件内容
        String s = handler.handle(str);
        // 2. 定义正则表达式删除目录标题行及其紧随的一行正文
        // (?m)          : 启用多行模式，让 ^ 和 $ 能匹配每一行的开始和结束
        // ^##\s*目\s*录.* : 匹配以 "##" 开头，中间包含 "目" 和 "录" (允许中间有空格) 的整行标题
        // \R            : 匹配标题行末尾的换行符
        // .* : 匹配下一行整行内容 (即合并后的目录正文)
        // \R?           : 可选地匹配目录正文后的换行符，避免删除后留下空行
        String catalogPattern = "(?m)^##\\s*目\\s*录.*\\R.*\\R?";

        // 3. 将匹配到的标题行和目录内容行替换为空字符串并返回结果
        return s.replaceAll(catalogPattern, "");
    }

    public void printTitleLevel(String originText) {
        String s = initOriginText(originText);
        Node node = extractStructureTree(s);
        printStructureTree(node);
    }
}
