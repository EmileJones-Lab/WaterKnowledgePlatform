package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

class SubTitleLevelCorrector : MarkdownFileHandler {
    companion object {
        private val bracketPairRegex = """^#*\s?（\d+）""".toRegex()
        private val bracketSingleRegex = """^#*\s?\d+）""".toRegex()
    }


    private var markdownLines: MutableList<String> = ArrayList()
    private var isBracketPair: Boolean = false
    private var isBracketSingle: Boolean = false
    private var level: Int = 0
    private var index: Int = 0

    override fun handle(markdownText: String): String {
        init(markdownText)
        while (index < markdownLines.size) {
            handleLine()
            index++
        }
        return markdownLines.filter { it.trim().isNotBlank() }.joinToString(separator = "\n")
    }

    private fun init(markdownText: String) {
        markdownLines = markdownText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableList()
        isBracketPair = false
        isBracketSingle = false
        level = 0
        index = 0
    }

    private fun handleTitle() {
        level = markdownLines[index].count { it == '#' }
        isBracketSingle = false
        isBracketPair = false
    }

    private fun handleBracketSingle() {
        if (!isBracketSingle) {
            level++
            isBracketSingle = true
        }
        markdownLines[index] = "#".repeat(level) + " " + markdownLines[index].replace('#', ' ')
    }

    private fun handleBracketPair() {
        if (isBracketSingle) {
            isBracketSingle = false
            level--
        }
        if (!isBracketPair) {
            level++
            isBracketPair = true
        }
        markdownLines[index] = "#".repeat(level) + " " + markdownLines[index].replace('#', ' ')
    }

    private fun handlerCommonText() {
        markdownLines[index] = markdownLines[index].trim()
    }

    private fun handleLine() {
        if (markdownLines[index].contains(bracketPairRegex)) {
            handleBracketPair()
        } else if (markdownLines[index].contains(bracketSingleRegex)) {
            handleBracketSingle()
        } else if (markdownLines[index].startsWith("#")) {
            handleTitle()
        } else {
            handlerCommonText()
        }

    }
}