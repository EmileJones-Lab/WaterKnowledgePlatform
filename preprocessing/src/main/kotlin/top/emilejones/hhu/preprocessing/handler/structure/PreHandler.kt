package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 将除了第一行外的所有`#`全部删除
 * @author EmileJones
 */
class PreHandler : MarkdownFileHandler {
    override fun handle(markdownText: String): String {
        val lines = markdownText.lines()
            .map { it.trimIndent() }
            .filter { it.isNotBlank() }
            .toMutableList()
        var index = 1
        while (index < lines.size) {
            if (lines[index].startsWith("#") ) {
                lines[index] = lines[index].replace('#', ' ').trimIndent()
            }
            index++
        }
        return lines.joinToString("\n")
    }
}