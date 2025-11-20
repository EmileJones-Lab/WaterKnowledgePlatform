package top.yeyezhi.hhu.preprocessing.structure;

import top.yeyezhi.hhu.preprocessing.structure.enums.TitleType;
import top.yeyezhi.hhu.preprocessing.structure.tree.Node;

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
 */
public class TitleTreeExtractor extends AbstractTitleTreeExtractor {

    private String[] lines;

    @Override
    protected String initOriginText(String originText) {
        String[] textLines = originText.split("\\R");
        StringBuilder result = new StringBuilder();
        for (String line : textLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                result.append(line).append(System.lineSeparator());
                continue;
            }

            boolean lineChanged = false;
            for (TitleType type : TitleType.values()) {
                if (type == TitleType.NilType || type.getTitleRegex() == null) {
                    continue;
                }

                String patternString = type.getTitleRegex().replace("[\\s].*", "");
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(trimmedLine);

                if (matcher.lookingAt()) {
                    int matchEnd = matcher.end();
                    if (trimmedLine.length() > matchEnd && !Character.isWhitespace(trimmedLine.charAt(matchEnd))) {
                        result.append(trimmedLine.substring(0, matchEnd))
                              .append(" ")
                              .append(trimmedLine.substring(matchEnd))
                              .append(System.lineSeparator());
                        lineChanged = true;
                        break;
                    }
                }
            }

            if (!lineChanged) {
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
            if (lineToNode.containsKey(i)) {
                Node node = lineToNode.get(i);
                int depth = getDepth(node);
                String prefix = String.join("", Collections.nCopies(depth, "#"));

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
}
