package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 将以 `# 目录` 开头的 markdown 目录段落转换为正文：
 * 1. 把 `# 目录` 降级为 `## 目录`
 * 2. 将目录下所有以 `#`或`##` 或 `###` 或 `####` 开头的小节转换为正文（去除 # 号）
 * 3. 替换结束于下一个真正正文章节（如 `## 1 总则`）前
 *
 * @author EmileJones
 */
class CatalogTitleLevelCorrector : MarkdownFileHandler {
    private val catalogRegex = """#*\s*目\s*录""".toRegex()
    private val firstTitleRegex = """^#+\s*((\d+\.?)|([一二三四五六七八九十])、)\s*\D+""".toRegex()

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
            if (lines[nowIndex].matches(firstTitleRegex)) {
                if (isFirst)
                    isFirst = false
                else
                    return nowIndex
            }
            nowIndex++
        }
        throw RuntimeException("没找见目录结束位置")
    }


}