package top.emilejones.hhu.preprocessing.handler

import top.emilejones.hhu.preprocessing.handler.structure.*

/**
 * 此文件可以使符合`1`，`1.1`，`1.1.1`，`（1）`，`1）`这种格式的文件变得层次正确
 *
 * @author EmileJones
 */
class MarkdownStructureCorrector : MarkdownFileHandler {
    companion object {
        private val chain: List<MarkdownFileHandler> =
            listOf(
                PreHandler(),
                TitleLevelCorrector(),
                SubTitleLevelCorrector(),
                CatalogTitleLevelCorrector(),
                MergeTitleToText(),
            )
    }

    override fun handle(markdownText: String): String {
        var text = markdownText

        for (handler in chain) {
            text = handler.handle(text)
        }

        return text
    }
}