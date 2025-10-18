package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 将以 `# 目录` 开头的 markdown 目录段落转换为正文： 相比于CatalogTitleLevelCorrector做了如下改进
 * 1. 可是识别出目录中如 “1 前 言.. 1 - ”的正文，也就是带有 `-`的内容
 *  注： looksLikePseudoToc()函数是专门识别出目录里面的正文末尾，如有其他格式的末尾可自行添加
 *
 * @author yeyezhi
 */
class CatalogTitleLevelCorrectorPlus : MarkdownFileHandler {
    private val catalogRegex = """#*\s*目\s*录""".toRegex()
    private val firstTitleRegex = """^#*\s*((\d+\.?)|([一二三四五六七八九十])、)\s*\D+""".toRegex()

    override fun handle(markdownText: String): String {
        if (!markdownText.contains(catalogRegex))
            return markdownText;
        val lines = markdownText.lines().map { it.trimIndent() }.filter { it.isNotBlank() }.toMutableList()
        val startIndex = findCatalogMainTextStartLineIndexAndCorrectCatalogLevel(lines)
        val endIndex = findCatalogMainTextEndLineIndex(lines, startIndex)
        correctCatalogMainTextLevel(lines, startIndex, endIndex)
        val firstSequence = lines.subList(0, startIndex + 1)
        val secondSequence = lines.subList(startIndex + 1, endIndex)
        val thirdSequence = lines.subList(endIndex, lines.size)
        return listOf(firstSequence, listOf(secondSequence.joinToString(separator = "\t")), thirdSequence)
            .flatten()
            .joinToString(separator = "\n")
    }

    private fun correctCatalogMainTextLevel(lines: MutableList<String>, catalogIndex: Int, endIndex: Int) {
        for (i in catalogIndex + 1..<endIndex) {
            lines[i] = lines[i].replace('#', ' ').trimIndent()
        }
    }

    private fun findCatalogMainTextStartLineIndexAndCorrectCatalogLevel(lines: MutableList<String>): Int {
        for (index in lines.indices) {
            if (!lines[index].matches(catalogRegex))
                continue
            lines[index] = "## " + lines[index].replace('#', ' ').trimIndent()
            return index
        }
        throw RuntimeException("没找见目录开始位置")
    }

    private fun findCatalogMainTextEndLineIndex(lines: MutableList<String>, catalogIndex: Int): Int {
        var nowIndex = catalogIndex + 1
        var isFirst = true
        while (nowIndex < lines.size) {
            val curr = lines[nowIndex]
            if (firstTitleRegex.matches(curr) && !looksLikePseudoToc(curr)) {
                if (isFirst){
                    // 如果不是伪标题，直接当成正文
                    return nowIndex
                }
                else {
                    return nowIndex
                }
            }
            nowIndex++
        }
        throw RuntimeException("没找见目录结束位置")
    }

    private fun looksLikePseudoToc(line: String): Boolean {
        val s = line.trim()
        // 目录里的常见伪标题尾巴：省略号、点号
        if (s.contains("..") || s.contains("……")) return true
        // 目录里的页码数字（结尾是数字或数字+横线）
        if (s.matches(Regex(""".*?\d+\s*[-－—]?\s*$"""))) return true
        // 目录里常见“.”、"…"结尾
        if (s.endsWith(".") || s.endsWith("…")) return true
        return false
    }

}