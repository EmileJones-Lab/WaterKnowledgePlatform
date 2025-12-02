package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler
import java.util.regex.Pattern

/**
 * 规范`1、 ` => `1.1` => `1.1.1` => `1、` 层级
 *
 *  @author yeyezhi
 */
class TitleLevelCorrectorPro : MarkdownFileHandler {

    companion object {
        // 匹配 "1、2、3、" 这样的中文数字开头
        private val DUNHAO_TOP: Pattern = Pattern.compile("^#*\\s*\\d+、.*")

        // 匹配 1.1、1.1.1 等
        private val DOT_PATTERN: Pattern = Pattern.compile("^#*\\s*\\d+(\\.\\d+)+.*")

        // 匹配 1、 这样的次顶层
        private val COMMA_PATTERN: Pattern = Pattern.compile("^#*\\s*\\d+、.*")
    }


    private var lastLevel = 0
    private var inCommaBlock = false // 标记是否在 "1、2、3、" 同级块中

    override fun handle(markdownText: String): String {
        lastLevel = 0
        inCommaBlock = false
        val sb = StringBuilder()

        for (rawLine in markdownText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val line = rawLine.trim { it <= ' ' }
            if (line.isEmpty()) continue

            var result = normalizeNumbering(line)

            if (DUNHAO_TOP.matcher(result).matches()) {
                // 中文顶层 => 二级标题
                lastLevel = 2
                inCommaBlock = false
                result = "#".repeat(lastLevel) + " " + result.replace("#", "").trim { it <= ' ' }
            } else if (DOT_PATTERN.matcher(result).matches()) {
                // 点号子标题 => 按 . 个数递进
                val m = Pattern.compile("\\d+(\\.\\d+)+").matcher(result)
                if (m.find()) {
                    var level = m.group().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size + 1
                    if (level > 6) level = 6
                    lastLevel = level
                    inCommaBlock = false
                    result = "#".repeat(level) + " " + result.replace("#", "").trim { it <= ' ' }
                }
            } else if (COMMA_PATTERN.matcher(result).matches()) {
                var level: Int
                if (lastLevel <= 2) {
                    // 文档开头/章层次 => 二级标题
                    level = 2
                    inCommaBlock = false
                } else {
                    // 如果前一个是点号标题 (>=3)，则作为子条目
                    if (!inCommaBlock) {
                        level = lastLevel + 1
                        inCommaBlock = true
                    } else if (inCommaBlock) {
                        // 同一组 1、2、3、保持同级
                        level = lastLevel
                    } else {
                        // 默认兜底：还是二级
                        level = 2
                    }
                }
                if (level > 6) level = 6
                lastLevel = level
                result = "#".repeat(level) + " " + result.replace("#", "").trim()
            }

            sb.append(result).append("\n")
        }

        return sb.toString().trim { it <= ' ' }
    }

    /** 清理 1 .1 → 1.1 / 1. 1 → 1.1 等情况  */
    private fun normalizeNumbering(text: String): String {
        return text.replace("(\\d+)\\s*\\.\\s*(\\d+)".toRegex(), "$1.$2")
    }
}
