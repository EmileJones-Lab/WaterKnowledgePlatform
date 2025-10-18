package top.yeyezhi.hhu.preprocessing.handler.structure;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;

import java.util.*;
import java.util.regex.*;

/**
 * 处理混合格式标题：
 * 主要用来修复遵循`一、` => `（二）` => `3.` => `（4）` => `5）`这样格式的markdown文档，这个class做了以下任务
 * 1. 将`# 一、`变为`## 一、`，将`# 1.`变为`#### 1.`，如此类推
 *
 * @author yeyezhi
 */
public class MixedTitleLevelCorrector implements MarkdownFileHandler {

    private static final Pattern CHINESE_NUM_PATTERN = Pattern.compile("^[一二三四五六七八九十百千]+、");
    private static final Pattern CHINESE_BRACKET_PATTERN = Pattern.compile("^（[一二三四五六七八九十百千]+）");
    private static final Pattern ARABIC_DOT_PATTERN = Pattern.compile("^\\d+[.、]");
    private static final Pattern ARABIC_BRACKET_PATTERN = Pattern.compile("^（\\d+）");
    private static final Pattern ARABIC_SINGLE_BRACKET_PATTERN = Pattern.compile("^\\d+）");

    @Override
    public String handle(String markdownText) {
        List<String> lines = Arrays.stream(markdownText.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(correctLine(line));
        }
        return String.join("\n", result);
    }

    private String correctLine(String line) {
        int level = 0;

        if (CHINESE_NUM_PATTERN.matcher(line).find()) {
            level = 2; // 一级标题下的子标题
        } else if (CHINESE_BRACKET_PATTERN.matcher(line).find()) {
            level = 3;
        } else if (ARABIC_DOT_PATTERN.matcher(line).find()) {
            level = 4;
        } else if (ARABIC_BRACKET_PATTERN.matcher(line).find()) {
            level = 5;
        } else if (ARABIC_SINGLE_BRACKET_PATTERN.matcher(line).find()) {
            level = 6;
        }

        if (level > 0) {
            return "#".repeat(level) + " " + line.replace("#", "").trim();
        } else {
            return line;
        }
    }
}
