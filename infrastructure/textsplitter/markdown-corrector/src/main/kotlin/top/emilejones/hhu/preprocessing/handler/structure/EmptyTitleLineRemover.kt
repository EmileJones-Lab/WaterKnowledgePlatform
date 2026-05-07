package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 移除只包含 Markdown 标题标记而没有标题内容的空标题行。
 *
 * @author Claude
 */
class EmptyTitleLineRemover : MarkdownFileHandler {
    private val emptyHeadingRegex = Regex("""^#{1,6}\s*$""")

    override fun handle(markdownText: String): String {
        return markdownText
            .lines()
            .filterNot { it.trim().matches(emptyHeadingRegex) }
            .joinToString(separator = "\n")
    }
}
