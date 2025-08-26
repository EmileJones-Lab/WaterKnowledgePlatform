package top.emilejones.hhu.spliter

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.lang.IllegalArgumentException

object HtmlTableSplitter : StringSplitter {


    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        if (!text.startsWith("<table>"))
            return Result.failure(IllegalArgumentException("The text must be the HTML table format"))
        if (text.length <= maxSequenceLength)
            return Result.success(listOf(text))

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
            chunk.lines()
                .map { it.trimIndent() }
                .filter { it.isNotBlank() }
                .joinToString(separator = "")
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