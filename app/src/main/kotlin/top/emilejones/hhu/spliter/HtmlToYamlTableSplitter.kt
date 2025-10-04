package top.emilejones.hhu.spliter

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object HtmlToYamlTableSplitter : StringSplitter {

    /**
     * 此方法用来将Html table转换为Yaml table。
     * 如果将Html table转换为Yaml table后长度超过maxSequenceLength，则会拆分Yaml table为多个片段，每个片段都有title属性
     * 如果一行的长度长于maxSequenceLength，那么此方法不会切分此行，会返回超过maxSequenceLength长度的片段。
     * @see main 看一个demo来了解Html table转换为Yaml table后的格式
     * @param text 需要转换的文本
     * @param maxSequenceLength 每个片段期望的最大长度
     * @return 切分后的结果，不保证每一个片段都小于指定的最大长度
     */
    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        if (!text.contains("<table"))
            return Result.failure(IllegalArgumentException("The text must be in HTML table format"))

        return runCatching {
            val doc = Jsoup.parse(text)
            val tables = doc.select("table")
            if (tables.isEmpty()) return Result.success(emptyList())

            val yamlChunks = mutableListOf<String>()
            for (table in tables) {
                yamlChunks.addAll(splitTableToYaml(table, maxSequenceLength))
            }
            yamlChunks
        }
    }

    // 拆分单个 HTML table → 多个 YAML table
    private fun splitTableToYaml(table: Element, maxLen: Int): List<String> {
        val rows: Elements = table.select("tr")
        if (rows.isEmpty()) return emptyList()

        val headerCells = rows.first()?.select("th, td")?.map { it.text() } ?: emptyList()
        val dataRows = rows.drop(1)

        val result = mutableListOf<String>()
        var currentRows = mutableListOf<String>()
        var currentLength = estimateHeaderLength(headerCells)

        for (row in dataRows) {
            val rowYaml = convertRowToYaml(row)
            val rowLength = rowYaml.length + 4 // 缩进和格式开销

            // 如果加上当前行会超过限制，就先收集成一个完整表格
            if (currentLength + rowLength > maxLen && currentRows.isNotEmpty()) {
                result.add(buildYamlTable(headerCells, currentRows))
                currentRows = mutableListOf()
                currentLength = estimateHeaderLength(headerCells)
            }

            currentRows.add(rowYaml)
            currentLength += rowLength
        }

        // 收尾
        if (currentRows.isNotEmpty()) {
            result.add(buildYamlTable(headerCells, currentRows))
        }

        return result
    }

    // 构造完整的 YAML 表格
    private fun buildYamlTable(headers: List<String>, rowList: List<String>): String {
        val sb = StringBuilder()
        sb.append("table:\n")
        if (headers.isNotEmpty()) {
            sb.append("  headers: [")
            sb.append(headers.joinToString(", ") { "\"$it\"" })
            sb.append("]\n")
        }
        sb.append("  rows:\n")
        rowList.forEach { sb.append("    - $it\n") }
        return sb.toString()
    }

    // 单行转 YAML
    private fun convertRowToYaml(row: Element): String {
        val cells = row.select("td, th").map { cell ->
            val rowspan = cell.attr("rowspan")
            val colspan = cell.attr("colspan")
            val value = cell.text()
            if (rowspan.isNotBlank() || colspan.isNotBlank()) {
                buildString {
                    append("{ value: \"$value\"")
                    if (rowspan.isNotBlank()) append(", rowspan: $rowspan")
                    if (colspan.isNotBlank()) append(", colspan: $colspan")
                    append(" }")
                }
            } else {
                "\"$value\""
            }
        }
        return "[${cells.joinToString(", ")}]"
    }

    // 粗略估算 headers 的长度
    private fun estimateHeaderLength(headers: List<String>): Int {
        if (headers.isEmpty()) return "table:\n".length
        return "table:\n  headers: [${headers.joinToString(", ")}]\n  rows:\n".length
    }
}

fun main() {
    val text = """
            <table>
                <tr>
                    <td>省</td>
                    <td>地市</td>
                    <td>行政区面积</td>
                </tr>
                <tr>
                    <td rowspan="2">河南</td>
                    <td>商丘</td>
                    <td>2035</td>
                </tr>
                <tr>
                    <td>永城</td>
                    <td>997</td>
                </tr>
                <tr>
                    <td rowspan="2">安徽</td>
                    <td></td>
                    <td></td>
                </tr>
                <tr>
                    <td></td>
                    <td>2484</td>
                </tr>
                <tr>
                    <td>江苏</td>
                    <td>徐州</td>
                    <td>105</td>
                </tr>
                <tr>
                    <td colspan="2">合计</td>
                    <td>6692</td>
                </tr>
            </table>
        """.trimIndent()
    val result = HtmlToYamlTableSplitter.split(text, 15)
    assert(result.isSuccess) {
        result.exceptionOrNull()?.message!!
    }
    val data = result.getOrThrow()
    println(data)
}