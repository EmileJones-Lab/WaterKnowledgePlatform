package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

class PreHandler : MarkdownFileHandler {
    private val titleRegex = "^#*\\s*\\d{1,2}(\\.\\d{1,2})*\\.?\\s?[^、\\d\\s]+".toRegex()
    private val catalogRegex = """^#*\s*目\s*录""".toRegex()
    override fun handle(markdownText: String): String {
        val lines = markdownText.lines()
            .map { it.trimIndent() }
            .filter { it.isNotBlank() }
            .toMutableList()
        var index = 1
        while (index < lines.size) {
            if (lines[index].startsWith("#") && !lines[index].contains(titleRegex) && !lines[index].contains(catalogRegex)) {
                lines[index] = lines[index].replace('#', ' ').trimIndent()
            }
            index++
        }
        return lines.joinToString("\n")
    }
}