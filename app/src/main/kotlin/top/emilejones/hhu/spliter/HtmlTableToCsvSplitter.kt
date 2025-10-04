package top.emilejones.hhu.spliter

import top.emilejones.hhu.spliter.exception.SplitException

@Deprecated("由于无法支持复杂表格，CSV的切割方式已被舍弃")
object HtmlTableToCsvSplitter : StringSplitter {
    private val tableRegex =
        Regex("<table.*?>(.*?)</table>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val trRegex = Regex("<tr.*?>(.*?)</tr>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val thRegex = Regex("<th.*?>(.*?)</th>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val tdRegex = Regex("<td.*?>(.*?)</td>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))


    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        if (tableRegex.find(text) == null)
            return Result.failure(SplitException("Not in HTML format"))
        if (thRegex.find(text) != null)
            return Result.failure(SplitException("<th></th> is not yet implemented"))
        val tableData = parse(text)

        val csvString = convertToCsv(tableData)
        if (csvString.length < maxSequenceLength)
            return Result.success(listOf(csvString))

        val splitCsvString = splitCsv(csvString)
        if (splitCsvString.count { it.length >= maxSequenceLength } > 0)
            return Result.failure(SplitException("Can't convert html to csv"))
        return Result.success(splitCsvString)
    }

    /**
     * 读取html并解析为List<List<String>>，目前只能解析没有<th></th>的HTML文本
     * @param table HTML中的<table></table>部分
     * @return 数据表
     */
    private fun parse(table: String): List<List<String>> {
        return trRegex.findAll(tableRegex.find(table)!!.groupValues[1])
            .map { trMatch ->
                tdRegex.findAll(trMatch.groupValues[1]).map { tdMatch ->
                    tdMatch.groupValues[1].trim()
                }.toList()
            }.toList()
    }


    private fun convertToCsv(data: List<List<String>>): String {
        return data.map { row -> row.map { "\"$it\"" } }
            .joinToString(separator = "\n") { it.joinToString(separator = ",") }
    }

    private fun splitCsv(csv: String): List<String> {
        val lines = csv.split("\n").toMutableList()

        val halfRowNumber = (lines.size - 1) / 2

        val firstSeq = lines.subList(0, halfRowNumber + 1)
        val secondSeq = lines.subList(halfRowNumber + 1, lines.size)

        secondSeq.add(0, lines[0])

        return listOf(firstSeq.joinToString(separator = "\n"), secondSeq.joinToString(separator = "\n"))
    }
}