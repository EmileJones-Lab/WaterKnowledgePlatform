package top.emilejones.hhu.spliter

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.security.SecureRandom

/**
 * @author EmileJones
 */
@Deprecated("由于YAML Table的字符数依然很多，所以此方案被舍弃")
object HtmlToYamlTableSplitter : StringSplitter {

    private const val SHORT_ID_LEN = 8 // 可调整短 id 长度（8 通常已足够短且碰撞概率极低）
    private val BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val rnd = SecureRandom()

    /**
     * 如果将Html table转换为Yaml table后长度超过maxSequenceLength，则会拆分Yaml table为多个片段，每个片段都有title属性
     * 如果一行的长度长于maxSequenceLength，那么此方法不会切分此行，会返回超过maxSequenceLength长度的片段。
     * @see main 看一个demo来了解Html table转换为Yaml table后的格式
     * @param text 需要转换的文本
     * @param maxSequenceLength 每个片段期望的最大长度
     * @return 切分后的结果，不保证每一个片段都小于指定的最大长度
     */
    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        if (!text.contains("<table", ignoreCase = true))
            return Result.failure(IllegalArgumentException("The text must be in HTML table format"))

        if (maxSequenceLength < 80) // 一个保守的下限，太小的话无法容纳元信息
            return Result.failure(IllegalArgumentException("maxSequenceLength is too small"))

        return runCatching {
            val doc = Jsoup.parse(text)
            val tables = doc.select("table")
            if (tables.isEmpty()) return@runCatching emptyList<String>()

            val yamlChunks = mutableListOf<String>()
            for (table in tables) {
                yamlChunks.addAll(splitTableToYaml(table, maxSequenceLength))
            }
            yamlChunks
        }
    }

    // ---------- 核心：拆分单个 table 为若干片段（每个片段长度 <= maxLen） ----------
    private fun splitTableToYaml(table: Element, maxLen: Int): List<String> {
        val rows: Elements = table.select("tr")
        if (rows.isEmpty()) return emptyList()

        val headerCells = rows.first()?.select("th, td")?.map { it.text() } ?: emptyList()
        val dataRows = rows.drop(1)

        val tableId = generateShortId(SHORT_ID_LEN)
        val result = mutableListOf<String>()

        // 先构造 header 模板并检查长度
        val headerOnlyStr = buildHeaderOnly(headers = headerCells, tableId = tableId)
        if (headerOnlyStr.length > maxLen) {
            // 极端情况：header 本身太长，尝试分割 header（做最简单的处理：逐列分片）
            val headerFragments = splitHeadersToFragments(headerCells, tableId, maxLen)
            result.addAll(headerFragments)
            // header 已经被拆分，后续片段不需要重复 header
        }

        var currentRows = mutableListOf<String>()
        var isFirstChunk = result.isEmpty() // 如果 header 已被单独放入 result，则第一个 chunk 已不是包含 header 的 chunk
        var currentLength = if (isFirstChunk) buildYamlTablePrefixLength(headerCells, tableId) else 0

        for (row in dataRows) {
            val singleRowItem = buildRowItemString(tableId, row) // 不拆时的整行表示 "- { __table_id: "TID", row: [...] }"

            if (singleRowItem.length <= maxLen) {
                // 整行可放入单个片段
                val fragments = listOf(singleRowItem)
                for (frag in fragments) {
                    if (isFirstChunk && currentRows.isEmpty() && currentLength + frag.length > maxLen) {
                        // header + this row 放不下：把 header 单独成片（如果尚未加入 result）
                        if (result.isEmpty() || !result.last().startsWith("table:")) {
                            result.add(buildHeaderOnly(headers = headerCells, tableId = tableId))
                        }
                        isFirstChunk = false
                        currentLength = 0
                    }
                    if (currentLength + frag.length > maxLen && currentRows.isNotEmpty()) {
                        // flush currentRows
                        if (isFirstChunk) {
                            result.add(buildYamlTable(headerCells, currentRows, tableId))
                            isFirstChunk = false
                        } else {
                            result.add(buildRowsOnly(currentRows))
                        }
                        currentRows = mutableListOf()
                        currentLength = 0
                    }
                    // 直接加入 frag
                    currentRows.add(frag)
                    currentLength += frag.length
                }
            } else {
                // 单行过长，需要拆分 -> 使用精确分片算法（保证每个片段长度 <= maxLen）
                val rowYamlRaw = convertRowToYaml(row) // 比如: ["张三", "很长的文本...", ...]
                val splitFrags = splitLongRowToFragments(rowYamlRaw, maxLen, tableId)
                for (frag in splitFrags) {
                    // frag already includes "- { __table_id: ..., ... }"
                    if (isFirstChunk && currentRows.isEmpty() && currentLength + frag.length > maxLen) {
                        // 把 header 单独成片
                        if (result.isEmpty() || !result.last().startsWith("table:")) {
                            result.add(buildHeaderOnly(headers = headerCells, tableId = tableId))
                        }
                        isFirstChunk = false
                        currentLength = 0
                    }
                    if (currentLength + frag.length > maxLen && currentRows.isNotEmpty()) {
                        // flush currentRows
                        if (isFirstChunk) {
                            result.add(buildYamlTable(headerCells, currentRows, tableId))
                            isFirstChunk = false
                        } else {
                            result.add(buildRowsOnly(currentRows))
                        }
                        currentRows = mutableListOf()
                        currentLength = 0
                    }
                    currentRows.add(frag)
                    currentLength += frag.length
                }
            }
        }

        // flush remaining
        if (currentRows.isNotEmpty()) {
            if (isFirstChunk) {
                result.add(buildYamlTable(headerCells, currentRows, tableId))
            } else {
                result.add(buildRowsOnly(currentRows))
            }
        } else {
            // 如果没有 rows，但尚未把 header 丢入 result（例如表没有数据），则确保 header 入库
            if (result.isEmpty()) {
                result.add(buildHeaderOnly(headers = headerCells, tableId = tableId))
            }
        }

        // 最后做一次安全检查，确保每个片段长度 <= maxLen
        for (seg in result) {
            if (seg.length > maxLen) {
                throw IllegalStateException("Generated fragment exceeds maxLen (length=${seg.length}, max=$maxLen). Consider increasing maxLen.")
            }
        }

        return result
    }

    // ---------- ID 生成 ----------
    private fun generateShortId(length: Int): String {
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(BASE62[rnd.nextInt(BASE62.length)])
        }
        return sb.toString()
    }

    // ---------- 构造 YAML 片段的辅助方法 ----------
    private fun buildYamlTable(headers: List<String>, rowList: List<String>, tableId: String): String {
        val sb = StringBuilder()
        sb.append("table:\n")
        sb.append("  table_id: \"$tableId\"\n")
        if (headers.isNotEmpty()) {
            sb.append("  headers: [")
            sb.append(headers.joinToString(", ") { "\"${escapeForYaml(it)}\"" })
            sb.append("]\n")
        }
        sb.append("  rows:\n")
        rowList.forEach { sb.append("    $it\n") } // rowList 中每项已经以 "- { ... }" 或 "- [..]" 形式存在（前导 - 会被缩进）
        return sb.toString()
    }

    private fun buildHeaderOnly(headers: List<String>, tableId: String): String {
        val sb = StringBuilder()
        sb.append("table:\n")
        sb.append("  table_id: \"$tableId\"\n")
        if (headers.isNotEmpty()) {
            sb.append("  headers: [")
            sb.append(headers.joinToString(", ") { "\"${escapeForYaml(it)}\"" })
            sb.append("]\n")
        }
        return sb.toString()
    }

    private fun buildRowsOnly(rowList: List<String>): String {
        // rowList 已经包含 "- { ... }" 前缀，每一项独占一行
        val sb = StringBuilder()
        rowList.forEach { sb.append(it).append("\n") }
        return sb.toString().trimEnd()
    }

    // 构造单行的 "行对象" 字符串（未拆分情况）
    private fun buildRowItemString(tableId: String, row: Element): String {
        val cells = row.select("td, th").map { cell ->
            val rowspan = cell.attr("rowspan")
            val colspan = cell.attr("colspan")
            val value = cell.text()
            if (rowspan.isNotBlank() || colspan.isNotBlank()) {
                buildString {
                    append("{ value: \"${escapeForYaml(value)}\"")
                    if (rowspan.isNotBlank()) append(", rowspan: $rowspan")
                    if (colspan.isNotBlank()) append(", colspan: $colspan")
                    append(" }")
                }
            } else {
                "\"${escapeForYaml(value)}\""
            }
        }
        // wrap into an object with table id so每个行片段都带 table id，便于检索
        return "- { __table_id: \"$tableId\", row: [${cells.joinToString(", ")}] }"
    }

    // convertRowToYaml 返回类似 ["a","b","c"] 形式（用于拆分时对原始行文本进行切片）
    private fun convertRowToYaml(row: Element): String {
        val cells = row.select("td, th").map { cell ->
            val rowspan = cell.attr("rowspan")
            val colspan = cell.attr("colspan")
            val value = cell.text()
            if (rowspan.isNotBlank() || colspan.isNotBlank()) {
                buildString {
                    append("{ value: \"${escapeForYaml(value)}\"")
                    if (rowspan.isNotBlank()) append(", rowspan: $rowspan")
                    if (colspan.isNotBlank()) append(", colspan: $colspan")
                    append(" }")
                }
            } else {
                "\"${escapeForYaml(value)}\""
            }
        }
        return "[${cells.joinToString(", ")}]"
    }

    // ---------- 将过长的行拆成多个片段（核心算法：二分查找每个片段最大可放长度） ----------
    private fun splitLongRowToFragments(rowYaml: String, maxLen: Int, tableId: String): List<String> {
        val parts = mutableListOf<String>()
        var pos = 0
        val rowId = generateShortId(SHORT_ID_LEN)

        while (pos < rowYaml.length) {
            val remaining = rowYaml.length - pos
            // 二分查找当前片可包含的最大字符数
            var low = 1
            var high = remaining
            var best = 0
            while (low <= high) {
                val mid = (low + high) ushr 1
                val slice = rowYaml.substring(pos, pos + mid)
                val frag = makeSplitFragmentString(tableId, rowId, parts.size + 1, (pos + mid) >= rowYaml.length, slice)
                if (frag.length <= maxLen) {
                    best = mid
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            if (best == 0) {
                // 说明 maxLen 太小，连最小的元信息都放不下
                throw IllegalArgumentException("maxSequenceLength is too small to contain fragment metadata. Increase maxSequenceLength.")
            }
            val slice = rowYaml.substring(pos, pos + best)
            val last = (pos + best) >= rowYaml.length
            val frag = makeSplitFragmentString(tableId, rowId, parts.size + 1, last, slice)
            parts.add(frag)
            pos += best
        }
        return parts
    }

    // 构造拆分片段字符串： "- { __table_id: "T", __row_id: "R", part: 1, last: false, text: "..." }"
    private fun makeSplitFragmentString(
        tableId: String,
        rowId: String,
        partIndex: Int,
        last: Boolean,
        rawTextSlice: String
    ): String {
        val escaped = escapeForYaml(rawTextSlice)
        return "- { __table_id: \"$tableId\", __row_id: \"$rowId\", part: $partIndex, last: $last, text: \"$escaped\" }"
    }

    // ---------- header 过长的分割（保守做法：按列分片） ----------
    private fun splitHeadersToFragments(headers: List<String>, tableId: String, maxLen: Int): List<String> {
        val fragments = mutableListOf<String>()
        val sb = StringBuilder()
        var current = mutableListOf<String>()
        for (h in headers) {
            val candidate = current + listOf(h)
            val headerStr = buildHeaderFragment(candidate, tableId)
            if (headerStr.length > maxLen) {
                // flush current
                if (current.isNotEmpty()) {
                    fragments.add(buildHeaderFragment(current, tableId))
                }
                current = mutableListOf(h)
            } else {
                current.add(h)
            }
        }
        if (current.isNotEmpty()) {
            fragments.add(buildHeaderFragment(current, tableId))
        }
        return fragments
    }

    private fun buildHeaderFragment(headers: List<String>, tableId: String): String {
        val sb = StringBuilder()
        sb.append("table:\n")
        sb.append("  table_id: \"$tableId\"\n")
        if (headers.isNotEmpty()) {
            sb.append("  headers: [")
            sb.append(headers.joinToString(", ") { "\"${escapeForYaml(it)}\"" })
            sb.append("]\n")
        }
        return sb.toString()
    }

    // ---------- 工具 ----------
    private fun escapeForYaml(s: String): String {
        // 简单转义：先转义反斜杠，再转义双引号，并把换行转义为 \n（保持单行字符串）
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    }

    // 估算第一个 chunk（包含 header 行头）占用的基础长度（用于初始 currentLength）
    private fun buildYamlTablePrefixLength(headers: List<String>, tableId: String): Int {
        // 计算 "table:\n  table_id: "TID"\n" + headers 部分长度 + "  rows:\n"
        val headerText = if (headers.isEmpty()) "" else headers.joinToString(", ") { "\"${escapeForYaml(it)}\"" }
        return ("table:\n  table_id: \"$tableId\"\n" + if (headers.isNotEmpty()) "  headers: [$headerText]\n  rows:\n" else "  rows:\n").length
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
    val result = HtmlToYamlTableSplitter.split(text, 100)
    assert(result.isSuccess) {
        result.exceptionOrNull()?.message!!
    }
    val data = result.getOrThrow()
    for (i in 0..<data.size) {
        println("第${i}个片段：")
        println(data[i])
    }
}