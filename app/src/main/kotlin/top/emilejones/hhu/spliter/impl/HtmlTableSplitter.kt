package top.emilejones.hhu.spliter.impl

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import top.emilejones.hhu.spliter.StringSplitter

/**
 * 根据文本长度拆分Html table文本为多个片段的实现类
 * @author EmileJones
 */
object HtmlTableSplitter : StringSplitter {

    /**
     * 切分Html table的方法
     *
     * ## 特性
     * - 切分Html的Table文本，如果文本超出了maxSequenceLength，则将表格切分成多个部分，每一个部分都拥有表头行。
     * - 如果一行的长度超出了maxSequenceLength，则此方法会切下这一行，尽管长度超出了maxSequenceLength。
     * - 所以此方法只会尽可能的将表格切分到maxSequenceLength以下，并不保证长度绝对小于maxSequenceLength。
     *
     * ## 示例
     * 输入:
     * ```html
     * <table>
     *     <tr><th>Name</th><th>Age</th></tr>
     *     <tr><td>Alice</td><td>20</td></tr>
     *     <tr><td>Bob</td><td>25</td></tr>
     *     <tr><td>Charlie</td><td>30</td></tr>
     * </table>
     * ```
     *
     * 输出：
     * ```json
     * [<table><tr><th>Name</th><th>Age</th></tr><tr><td>Alice</td><td>20</td></tr></table>,
     * <table><tr><th>Name</th><th>Age</th></tr><tr><td>Bob</td><td>25</td></tr></table>,
     * <table><tr><th>Name</th><th>Age</th></tr><tr><td>Charlie</td><td>30</td></tr></table>]
     * ```
     * @param text 需要切分的文本内容
     * @param maxSequenceLength 期望每个片段的最大长度
     *
     * @return 切分后的结果，不保证每一个片段长度绝对小于maxSequenceLength
     *
     * @throws IllegalArgumentException
     * 当一下情况时会发生报错：
     * - 不是HTML table文本
     * - Html table为空table，没有row
     */
    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        if (!text.startsWith("<table>")) return Result.failure(IllegalArgumentException("The text must be the HTML table format"))
        if (text.length <= maxSequenceLength) return Result.success(listOf(text))

        return runCatching {
            val doc = Jsoup.parse(text)
            val tables = doc.select("table")
            if (tables.isEmpty()) return Result.success(emptyList())

            val result = mutableListOf<String>()
            for (table in tables) {
                result.addAll(splitTable(table, maxSequenceLength))
            }
            result
        }
    }

    // 拆分单个 table
    private fun splitTable(table: Element, maxSequenceLength: Int): List<String> {
        val rows = table.select("tr")
        if (rows.isEmpty()) return emptyList()

        val headerRow = rows.first() ?: throw IllegalArgumentException("The <table> html must have at least one <th>")
        val dataRows = rows.drop(1)
        return splitRows(headerRow, dataRows, maxSequenceLength)
    }

    // 根据最大长度拆分数据行
    private fun splitRows(headerRow: Element, dataRows: List<Element>, maxSequenceLength: Int): List<String> {
        val result = mutableListOf<String>()
        var currentRows = mutableListOf<String>()
        var currentLength = headerRow.outerHtml().length + "<table></table>".length

        for (row in dataRows) {
            val rowHtml = row.outerHtml()
            if (currentLength + rowHtml.length > maxSequenceLength) {
                // 保存当前块
                result.add(buildTableHtml(headerRow.outerHtml(), currentRows))
                // 开始新块
                currentRows = mutableListOf()
                currentLength = headerRow.outerHtml().length + "<table></table>".length
            }
            currentRows.add(rowHtml)
            currentLength += rowHtml.length
        }

        // 添加最后一块
        if (currentRows.isNotEmpty()) {
            result.add(buildTableHtml(headerRow.outerHtml(), currentRows))
        }

        return result.map { chunk ->
            chunk.lines().map { it.trimIndent() }.filter { it.isNotBlank() }.joinToString(separator = "")
        }
    }

    // 根据标题行和数据行生成完整 table HTML
    private fun buildTableHtml(headerHtml: String, rowHtmlList: List<String>): String {
        val sb = StringBuilder()
        sb.append("<table>")
        sb.append(headerHtml)
        rowHtmlList.forEach { sb.append(it) }
        sb.append("</table>")
        return sb.toString()
    }
}