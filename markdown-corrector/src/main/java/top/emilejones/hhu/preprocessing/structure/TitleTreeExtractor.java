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
 * 该类负责：
 * 1. 预处理文本（移除目录、规范化标题格式）。
 * 2. 提取标题并构建层次结构树。
 * 3. 根据结构树重新生成规范化的标题（如自动修正 # 数量）。
 *
 * @author EmileJones
 * @author yeyezhi
 */
public class TitleTreeExtractor extends AbstractTitleTreeExtractor {

    private String[] lines;

    /**
     * 对原始文本进行预处理。
     * 步骤：
     * 1. 移除目录部分。
     * 2. 逐行规范化处理：去除冗余的起始 '#' 号、合并层级标题中的空格、确保标题标记后有空格。
     *
     * @param originText 原始 Markdown 文本
     * @return 预处理后的文本
     */
    @Override
    protected String initOriginText(String originText) {
        // 首先移除目录内容
        String haveNoCatalogText = removeCatalog(originText);
        String[] textLines = haveNoCatalogText.split("\\R");
        StringBuilder result = new StringBuilder();

        for (String line : textLines) {
            result.append(processSingleLine(line)).append(System.lineSeparator());
        }
        return result.toString();
    }

    /**
     * 处理单行文本，执行标题规范化逻辑。
     *
     * @param line 原始行文本
     * @return 规范化后的行文本
     */
    private String processSingleLine(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) {
            return line;
        }

        // 记录前导空格以便后续恢复
        int firstNonSpaceIndex = getLeadingSpaceCount(line);
        String workingLine = line;
        boolean lineChanged = false;

        // 1. 处理起始的多余 '#' 号（如果有）
        if (firstNonSpaceIndex < workingLine.length() && workingLine.charAt(firstNonSpaceIndex) == '#') {
            workingLine = workingLine.substring(0, firstNonSpaceIndex) + workingLine.substring(firstNonSpaceIndex + 1);
            trimmedLine = workingLine.trim();
            lineChanged = true;
        }

        String processedLine = trimmedLine;

        // 2. 处理层级标题格式（如 "1. 1. 1" -> "1.1.1"）
        String afterHierarchical = normalizeHierarchicalTitle(processedLine);
        if (!afterHierarchical.equals(processedLine)) {
            processedLine = afterHierarchical;
            lineChanged = true;
        }

        // 3. 确保标题标记与标题内容之间有空格
        String afterSpaceCorrection = ensureSpaceAfterTitleMarker(processedLine);
        if (!afterSpaceCorrection.equals(processedLine)) {
            processedLine = afterSpaceCorrection;
            lineChanged = true;
        }

        if (lineChanged) {
            String leadingSpaces = line.substring(0, firstNonSpaceIndex);
            return leadingSpaces + processedLine;
        }
        return line;
    }

    /**
     * 获取行首的空格数量（缩进量）。
     */
    private int getLeadingSpaceCount(String line) {
        int count = 0;
        while (count < line.length() && Character.isWhitespace(line.charAt(count))) {
            count++;
        }
        return count;
    }

    /**
     * 处理层级标题：删除数字和点号之间的空格（如 "1. 1. 1" -> "1.1.1"），但保留数字序列和文字之间的空格。
     */
    private String normalizeHierarchicalTitle(String trimmedLine) {
        // 匹配格式：数字序列（如 "1. 2. 3"）+ 空格 + 文字
        Pattern hierarchicalPattern = Pattern.compile("^((?:[0-9]+\\s*\\.\\s*){1,3}[0-9]+)(\\s+)(.+)$");
        Matcher hierarchicalMatcher = hierarchicalPattern.matcher(trimmedLine);
        if (hierarchicalMatcher.matches()) {
            String numberSequence = hierarchicalMatcher.group(1);
            String spaceAfterNumbers = hierarchicalMatcher.group(2);
            String textAfter = hierarchicalMatcher.group(3);

            // 将 "1. 2. 3" 转换为 "1.2.3"
            String normalizedSequence = numberSequence.replaceAll("(\\d+)\\s*\\.\\s*", "$1.");
            return normalizedSequence + spaceAfterNumbers + textAfter;
        }
        return trimmedLine;
    }

    /**
     * 遍历所有标题类型，确保匹配到的标题标记后紧跟一个空格。
     */
    private String ensureSpaceAfterTitleMarker(String processedLine) {
        for (TitleType type : TitleType.values()) {
            if (type == TitleType.NilType || type.getTitleRegex() == null) {
                continue;
            }

            // 获取不带内容匹配的正则前缀
            String patternString = type.getTitleRegex().replace("[\\s].*", "");
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(processedLine);

            if (matcher.lookingAt()) {
                int matchEnd = matcher.end();
                // 如果标记后没有空格且不是行尾，则插入空格
                if (processedLine.length() > matchEnd && !Character.isWhitespace(processedLine.charAt(matchEnd))) {
                    return processedLine.substring(0, matchEnd) + " " + processedLine.substring(matchEnd);
                }
            }
        }
        return processedLine;
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

    /**
     * 根据预处理后的文本构建标题树。
     * 利用栈或父节点引用的方式，根据标题类型匹配其在树中的位置。
     */
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

    /**
     * 根据生成的结构树修正文档中的标题层级。
     * 一级标题固定为 "# "，其余标题根据其在树中的深度确定 "#" 数量。
     */
    @Override
    protected String correctOriginTextByStructureTree(Node root) {
        Map<Integer, Node> lineToNode = new HashMap<>();
        mapLinesToNodes(root, lineToNode);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.lines.length; i++) {
            if (i == 0) {
                // 特殊处理文档首行（通常视为总标题）
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

    /**
     * 移除文档中的目录部分。
     * 目录通常由特定的处理器生成或标记，这里通过正则将其标题及内容移除。
     */
    private String removeCatalog(String str) {
        MarkdownFileHandler handler = new CatalogTitleLevelCorrectorPlus();
        String s = handler.handle(str);
        // 定义正则表达式删除目录标题行及其紧随的一行正文
        String catalogPattern = "(?m)^##\\s*目\\s*录.*\\R.*\\R?";
        return s.replaceAll(catalogPattern, "");
    }

    public void printTitleLevel(String originText) {
        String s = initOriginText(originText);
        Node node = extractStructureTree(s);
        printStructureTree(node);
    }
}
