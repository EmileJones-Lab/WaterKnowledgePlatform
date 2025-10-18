package top.yeyezhi.hhu.preprocessing.handler.structure;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 将符合格式`1）`和`（2）`的文本前面加上标题符号，标题等级会根据上一级标题等级来记
 * 例如：
 *      #### 1.1.2 父级标题
 *      ##### 1) 当前标题
 *      ######（1） 子标题
 * 注意：本类是SubTitleLevelCorrector的反序处理，只可以处理`1）`包含`（1）`格式的文档，如果`1）`包含`（1）`则本类不适用。
 *
 * @author yeyezhi
 */
public class SubTitleLevelCorrectorReverse implements MarkdownFileHandler {
    private static final String BRACKET_PAIR_REGEX = "^#*\\s?（\\d+）";
    private static final String BRACKET_SINGLE_REGEX = "^#*\\s?\\d+）";

    private List<String> markdownLines;
    private boolean isBracketPair;
    private boolean isBracketSingle;
    private int level;
    private int index;
    private static final int MAX_LEVEL = 6; // Markdown 最大六级标题

    @Override
    public String handle(String markdownText) {
        init(markdownText);
        while (index < markdownLines.size()) {
            handleLine();
            index++;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : markdownLines) {
            if (!line.trim().isEmpty()) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void init(String markdownText) {
        markdownLines = new ArrayList<>();
        for (String line : markdownText.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                markdownLines.add(trimmed);
            }
        }
        isBracketPair = false;
        isBracketSingle = false;
        level = 0;
        index = 0;
    }

    private void handleTitle() {
        level = countHashes(markdownLines.get(index));
        isBracketSingle = false;
        isBracketPair = false;
    }

    private void handleBracketSingle() {
        if (!isBracketSingle) {
            // 如果上一个不是 bracketSingle，就说明是第一次遇到，才加深一级
            if (!isBracketPair) {
                level++;
            }
            isBracketSingle = true;
            isBracketPair = false;
        }
        String cleaned = markdownLines.get(index).replaceFirst("^#+", "").trim();
        markdownLines.set(index, "#".repeat(Math.min(level, 6)) + " " + cleaned);
    }

    private void handleBracketPair() {
        if (!isBracketPair) {
            // 如果上一个是 bracketSingle，先回退一层
            if (isBracketSingle) {
                level--;
            }
            level++;
            isBracketPair = true;
            isBracketSingle = false;
        }
        String cleaned = markdownLines.get(index).replaceFirst("^#+", "").trim();
        markdownLines.set(index, "#".repeat(Math.min(level, 6)) + " " + cleaned);
    }


    private void handlerCommonText() {
        markdownLines.set(index, markdownLines.get(index).trim());
    }

    private void handleLine() {
        String line = markdownLines.get(index);
        if (line.matches(".*" + BRACKET_SINGLE_REGEX + ".*")) {  // 改成 find 模式
            handleBracketSingle();
        } else if (line.matches(".*" + BRACKET_PAIR_REGEX + ".*")) {
            handleBracketPair();
        } else if (line.startsWith("#")) {
            handleTitle();
        } else {
            handlerCommonText();
        }
    }

    private int countHashes(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == '#') count++;
        }
        return count;
    }
}
