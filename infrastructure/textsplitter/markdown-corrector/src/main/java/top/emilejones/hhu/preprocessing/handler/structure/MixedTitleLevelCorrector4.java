package top.emilejones.hhu.preprocessing.handler.structure;

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理混合格式标题：
 * 主要用来修复遵循`一、`  => `1.1` => `1.1.1` =>`1、`这样格式的markdown文档，这个class做了以下任务
 * 1. 将`# 一、`变为`## 一、`，将`# 1、`变为`##### 1、`，如此类推
 *
 * @author yeyezhi
 */
public class MixedTitleLevelCorrector4 implements MarkdownFileHandler {

    // 匹配 "一、二、三、" 这样的中文数字开头
    private static final Pattern CHINESE_TOP = Pattern.compile("^#*\\s*[一二三四五六七八九十]+、.*");

    // 匹配 1.1、1.1.1 等
    private static final Pattern DOT_PATTERN = Pattern.compile("^#*\\s*\\d+(\\.\\d+)+.*");

    // 匹配 1、 这样的次顶层
    private static final Pattern COMMA_PATTERN = Pattern.compile("^#*\\s*\\d+、.*");

    private int lastLevel = 0;
    private boolean inCommaBlock = false; // 标记是否在 "1、2、3、" 同级块中

    @Override
    public String handle(String markdownText) {
        lastLevel = 0;
        inCommaBlock = false;
        StringBuilder sb = new StringBuilder();

        for (String rawLine : markdownText.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String result = normalizeNumbering(line);

            if (CHINESE_TOP.matcher(result).matches()) {
                // 中文顶层 => 二级标题
                lastLevel = 2;
                inCommaBlock = false;
                result = "#".repeat(lastLevel) + " " + result.replace("#", "").trim();

            } else if (DOT_PATTERN.matcher(result).matches()) {
                // 点号子标题 => 按 . 个数递进
                Matcher m = Pattern.compile("\\d+(\\.\\d+)+").matcher(result);
                if (m.find()) {
                    int level = m.group().split("\\.").length + 1;
                    if (level > 6) level = 6;
                    lastLevel = level;
                    inCommaBlock = false;
                    result = "#".repeat(level) + " " + result.replace("#", "").trim();
                }

            } else if (COMMA_PATTERN.matcher(result).matches()) {
                int level;
                if (lastLevel <= 2) {
                    level = 2; // 顶层的 "1、" => 二级标题
                    inCommaBlock = false;
                } else {
                    // 如果进入了 1、2、3、 这种列表，强制四级标题
                    if (!inCommaBlock) {
                        inCommaBlock = true;
                    }
                    level = inCommaBlock ? 5 : lastLevel + 1;
                }
                if (level > 6) level = 6;
                lastLevel = level;
                result = "#".repeat(level) + " " + result.replace("#", "").trim();
            }

            sb.append(result).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 清理 1 .1 → 1.1 / 1. 1 → 1.1 等情况
     */
    private String normalizeNumbering(String text) {
        return text.replaceAll("(\\d+)\\s*\\.\\s*(\\d+)", "$1.$2");
    }
}
