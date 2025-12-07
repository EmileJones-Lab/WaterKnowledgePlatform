package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 规范`1 ` => `1.1` => `1.1.1` => `1、` 层级；只处理数字+顿号（1、2、）为子标题；
 * 避免将正文（年份等）误识别为标题；
 *
 *  @author yeyezhi
 */
class TitleLevelCorrectorPlus : MarkdownFileHandler {

    companion object {
        // 点号标题：1 / 1.1 / 1.1.1
        private val DOT_BLOCK = Regex("""^\s*(\d{1,2}(?:\s*[.．]\s*\d{1,2})*)\s+(.+)$""")
        // 顿号标题：1、 标题
        private val DUNHAO_ONLY = Regex("""^\s*(\d{1,2})、\s*(.+)$""")
        // 年份/大数字保护：2003 年…
        private val YEAR_GUARD = Regex("""^\s*\d{3,}(?!\s*[.．、])""")
    }

    private var lastDotLevel = 1           // 最近一次点号标题层级
    private var currentDunhaoLevel: Int? = null // 当前顿号分组层级

    override fun handle(markdownText: String): String {
        val lines = markdownText.lines()
            .map { it.trimIndent() }
            .filter { it.isNotBlank() }

        val out = StringBuilder()
        for ((idx, raw) in lines.withIndex()) {
            val line = normalizeNumbering(raw.trim())
            if (line.isBlank()) continue

            // 第一行文档名 → H1
            if (idx == 0) {
                out.appendLine(" $line")
                lastDotLevel = 1
                currentDunhaoLevel = null
                continue
            }

            // 年份正文保护
            if (YEAR_GUARD.containsMatchIn(line) && !DOT_BLOCK.matches(line)) {
                out.appendLine(line); continue
            }

            // 点号标题
            val mDot = DOT_BLOCK.matchEntire(line)
            if (mDot != null) {
                val numberingRaw = mDot.groupValues[1]
                val title = mDot.groupValues[2].trim()
                val normalizedNum = numberingRaw
                    .replace('．', '.')
                    .replace(Regex("""\s*[.]\s*"""), ".")
                val parts = normalizedNum.split('.').filter { it.isNotBlank() }

                if (!parts.all { it.length in 1..2 }) { // 避免年份误判
                    out.appendLine(line); continue
                }

                val level = parts.size + 1 // 从 H2 起步
                lastDotLevel = level
                currentDunhaoLevel = null // 重置顿号分组
                out.appendLine("#".repeat(level) + " " + normalizedNum + " " + title)
                continue
            }

            // 顿号标题
            val mDh = DUNHAO_ONLY.matchEntire(line)
            if (mDh != null) {
                if (currentDunhaoLevel == null) {
                    val base = if (lastDotLevel > 0) lastDotLevel + 1 else 2
                    currentDunhaoLevel = base
                }
                val level = currentDunhaoLevel!!
                out.appendLine("#".repeat(level) + " " + line)
                continue
            }

            // 其他 → 正文
            out.appendLine(line)
        }
        return out.toString().trimEnd()
    }

    private fun normalizeNumbering(text: String): String {
        var t = text
        // "1 . 1" → "1.1"
        t = t.replace(Regex("""(\d+)\s*[.．]\s*(\d+)"""), "$1.$2")
        // "1.1. 1" → "1.1.1"
        t = t.replace(Regex("""(\d+\.\d+)\.\s+(\d+)"""), "$1.$2")
        return t
    }
}
