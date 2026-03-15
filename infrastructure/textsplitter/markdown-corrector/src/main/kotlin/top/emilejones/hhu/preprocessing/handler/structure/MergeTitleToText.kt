package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler
import java.util.*

/**
 * 将连续的多个标题合并为正文
 * @author EmileJones
 */
class MergeTitleToText : MarkdownFileHandler {
    companion object {
        private const val TEXT_LEVEL = -1
        private const val STACK_BOTTOM = Int.MIN_VALUE
    }

    override fun handle(markdownText: String): String {
        val stack: Stack<Int> = Stack()

        val lines = markdownText.lines()
            .map { it.trimIndent() }
            .filter { it.isNotBlank() }
            .toMutableList()

        stack.push(STACK_BOTTOM)
        for (index in lines.indices) {
            val level = getLevel(lines[index])
            if (level < stack.peek())
                merge(lines, stack, index)
            stack.push(level)
        }
        merge(lines, stack, lines.size)

        return lines.joinToString(separator = "\n")
    }

    private fun merge(lines: MutableList<String>, stack: Stack<Int>, index: Int) {
        val initLevel = stack.peek()
        if (initLevel == STACK_BOTTOM || initLevel == TEXT_LEVEL)
            return

        var num = 1
        stack.pop()
        while (initLevel == stack.peek()) {
            num++
            stack.pop()
        }
        if (num == 1) {
            stack.push(initLevel)
            return
        }
        val startMergeIndex = (index - num)

        for (i in startMergeIndex..<index) {
            lines[i] = lines[i].replace('#', ' ').trimIndent()
        }
        stack.push(TEXT_LEVEL)
    }

    private fun getLevel(line: String): Int {
        val level = line.count { it == '#' }
        return if (level > 0) level else TEXT_LEVEL
    }
}