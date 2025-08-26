package top.emilejones.hhu.preprocessing.handler

import top.emilejones.hhu.preprocessing.handler.structure.*

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