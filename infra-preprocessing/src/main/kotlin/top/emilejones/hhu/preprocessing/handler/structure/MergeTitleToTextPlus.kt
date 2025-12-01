package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler
import java.util.Stack

/**
 * 将连续的多个标题合并为正文，此类设计相比于原有MergeTitleToText代码，改动为：
 *
 * 1.能继续判断出连续正文的序号是同一种，防止出现（1）和2.3.2.1因为都是五级标题并刚好连续，就都转为正文这种情况。
 * 2.解决类似 ## 10)《水量分配工作方案》（2011 年）；
 *          ## 11)《水权制度建设框架》（2005 年）；这种英文符号的连续标题。
 * 3.还可以合并1、2、或者1. 2.这样的连续标题
 *
 * @author yeyezhi
 */
class MergeTitleToTextPlus : MarkdownFileHandler {
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
        if (initLevel == STACK_BOTTOM || initLevel == TEXT_LEVEL) return

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

        val startMergeIndex = index - num
        var groupStart = startMergeIndex
        var currentPattern = getNumberPattern(lines[groupStart])

        for (i in startMergeIndex until index) {
            val pattern = getNumberPattern(lines[i])
            if (pattern != currentPattern) {
                // 处理前一个小组
                applyMergeGroup(lines, groupStart, i, currentPattern)
                // 开始新的小组
                groupStart = i
                currentPattern = pattern
            }
        }
        // 处理最后一个小组
        applyMergeGroup(lines, groupStart, index, currentPattern)

        stack.push(TEXT_LEVEL)
    }

    private fun applyMergeGroup(lines: MutableList<String>, start: Int, end: Int, pattern: String) {
        if (end - start <= 1) return // 只有一个，不合并
        for (i in start until end) {
            lines[i] = lines[i].replace('#', ' ').trimIndent()
        }

    }


    // 判断序号模式（最简化版）
    private fun getNumberPattern(line: String): String {
        val content = line.replace("#", "").trim().replace(" ", "")
        return when {
            content.matches(Regex("""^\d+、.*""")) -> "num-comma"        // 1、...
            content.matches(Regex("""^\d+(\.\d+)+.*""")) -> "dot"          // 2.3.2.1
            content.matches(Regex("""^\d+\.?.*""")) -> "num"               // 1. / 2.
            content.matches(Regex("""^（\d+）.*""")) -> "bracket-cn"       // （1）
            content.matches(Regex("""^\(\d+\).*""")) -> "bracket-en"       // (1)
            content.matches(Regex("""^\d+）.*""")) -> "singleRight-cn"     // 1）
            content.matches(Regex("""^\d+\).*""")) -> "singleRight-en"     // 1)
            else -> "other"
        }
    }

    private fun getLevel(line: String): Int {
        val level = line.count { it == '#' }
        return if (level > 0) level else TEXT_LEVEL
    }


}