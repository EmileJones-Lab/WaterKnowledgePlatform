package top.emilejones.hhu.spliter

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import top.emilejones.hhu.spliter.exception.SplitException

/**
 * @author EmileJones
 */
object HtmlTableToCsvSplitter : StringSplitter {
    /**
     * 将 HTML 表格解析并切分为多个 CSV 文本片段。
     *
     * 功能说明：
     * 1. 支持解析一个或多个 {@code <table>} 标签。
     * 2. 每个表格会被展开为二维矩阵（自动处理 {@code rowspan}/{@code colspan}）。
     * 3. 转换为符合 CSV 规范的文本（每个单元格使用双引号包裹，并转义内部双引号）。
     * 4. 根据 {@code maxSequenceLength} 将 CSV 按行切分为若干片段，每个片段长度不超过上限。
     * 5. 若表格包含表头（由 {@code <th>} 或 {@code <thead>} 决定），表头将在每个片段中重复，以保证独立性。
     *
     * 运行步骤：
     * 1. 解析 HTML，提取所有 `<table>` 元素。
     * 2. 对每个表格调用 `parseTableWithHeaderInfo()`：
     *    - 展开跨行跨列单元格；
     *    - 得到完整二维表格矩阵；
     *    - 返回表格内容及表头行数。
     * 3. 将表格矩阵转换为 CSV 行（对单元格内容加引号、转义）。
     * 4. 若整表长度 ≤ `maxSequenceLength`，直接作为单个 chunk。
     * 5. 若超长，则按行拆分：
     *    - 表头行在每个片段前重复；
     *    - 保证每个 chunk 长度不超限；
     *    - 每行都完整，不拆分中途。
     * 6. 检查所有生成的 chunk，若有任何超过限制则报错。
     *
     * @see main 可以运行main函数去查看生成的效果
     *
     * @param text HTML 字符串，必须包含至少一个 {@code <table>} 标签。
     * @param maxSequenceLength 限制每个输出 CSV 片段的最大字符数。
     *
     * @return 以下两种结果：
     *         1. {@code Result.success(List<String>)}：每个元素是一个自包含的 CSV 字符串片段。
     *         2. {@code Result.failure(SplitException)}：当 HTML 无表格、表格行过长或解析失败时返回错误信息。
     *
     * @throws SplitException 当以下情况发生时：
     *         1. HTML 中没有 {@code <table>}。
     *         2. 某一行（或表头整体）长度超出 {@code maxSequenceLength}。
     *         3. 捕获到运行时异常并包装为 {@code SplitException}。
     */
    override fun split(text: String, maxSequenceLength: Int): Result<List<String>> {
        try {
            val doc = Jsoup.parse(text)
            val tables = doc.select("table")
            if (tables.isEmpty()) {
                return Result.failure(SplitException("No <table> found"))
            }

            val allChunks = mutableListOf<String>()

            for (table in tables) {
                // 解析单个表格，得到矩阵 + 表头行数
                val parsed = parseTableWithHeaderInfo(table)
                val grid = parsed.grid
                val headerRowCount = parsed.headerRowCount

                if (grid.isEmpty()) {
                    // 空表格跳过
                    continue
                }

                // 转成 CSV 行（每行一个字符串）
                val csvLines = grid.map { row ->
                    row.joinToString(separator = ",") { cell ->
                        // 处理 CSV 双引号转义并加双引号包裹
                        "\"${cell.replace("\"", "\"\"")}\""
                    }
                }

                // 如果表格整体长度小于阈值，直接加入
                val wholeCsv = csvLines.joinToString("\n")
                if (wholeCsv.length <= maxSequenceLength) {
                    allChunks.add(wholeCsv)
                    continue
                }

                // 需要切片：以 header 行数（可能为0）为准，把 header 每片重复
                val headerLines = if (headerRowCount > 0) csvLines.subList(0, headerRowCount) else emptyList()
                val dataLines = if (headerRowCount > 0) csvLines.subList(headerRowCount, csvLines.size) else csvLines

                // 检查单行长度是否超过限制：若某行自身 > max => 无法切分
                val problematicLine = (headerLines + dataLines).firstOrNull { it.length > maxSequenceLength }
                if (problematicLine != null) {
                    return Result.failure(SplitException("A single CSV line exceeds maxSequenceLength; cannot split reliably"))
                }

                // 若 header 本身就超过限制，则失败
                val headerCombined = if (headerLines.isNotEmpty()) headerLines.joinToString("\n") else ""
                if (headerCombined.isNotEmpty() && headerCombined.length > maxSequenceLength) {
                    return Result.failure(SplitException("Header rows combined exceed maxSequenceLength; cannot create self-contained chunks"))
                }

                // 构建分片：每个片段以 header 开头（如果存在），然后尽量多装数据行但不超长
                var i = 0
                while (i < dataLines.size) {
                    val sb = StringBuilder()
                    if (headerLines.isNotEmpty()) {
                        sb.append(headerCombined)
                    } else {
                        // 当没有 header 时，确保不会以空开头
                    }

                    // 如果 header 非空并且有后续数据，需要在 header 行后加换行（以便拼接行）
                    if (sb.isNotEmpty()) sb.append("\n")

                    // 填充数据行
                    var addedAtLeastOne = false
                    while (i < dataLines.size) {
                        val nextLine = dataLines[i]
                        val wouldBeLength = sb.length + nextLine.length + if (sb.isEmpty()) 0 else 1 // +1 for newline
                        if (wouldBeLength <= maxSequenceLength) {
                            if (sb.isNotEmpty() && !addedAtLeastOne) {
                                // sb 里已放 header 但尚未放数据（header + first data）， header 已有 trailing \n
                            }
                            if (sb.isNotEmpty() && addedAtLeastOne) {
                                sb.append("\n")
                            }
                            if (sb.isEmpty() && addedAtLeastOne) {
                                sb.append("\n")
                            }
                            if (sb.isEmpty() && !addedAtLeastOne) {
                                // sb empty and no appended yet (no header)
                            }
                            if (sb.isNotEmpty() && !addedAtLeastOne && headerLines.isEmpty()) {
                                // nothing special
                            }
                            // Append line (if sb currently not empty and last char not newline, add newline)
                            if (sb.isNotEmpty() && sb.last() != '\n') sb.append("\n")
                            sb.append(nextLine)
                            i++
                            addedAtLeastOne = true
                        } else {
                            // 放不下更多行，结束当前 chunk
                            break
                        }
                    }

                    // 如果由于某些原因（例如 header 存在但即使没有数据也 length > max），上面会被阻断，检查
                    val chunkStr = sb.toString().trimEnd('\n')
                    if (chunkStr.isEmpty()) {
                        // 说明我们没能在这个循环里放入任何数据（可能 header=="" 且 nextLine 长度单独超过 max 已被前面排除）
                        return Result.failure(SplitException("Cannot pack any lines into chunk; splitting failed"))
                    }
                    allChunks.add(chunkStr)
                }
            }

            // 最终检查：所有片段均满足长度限制（保险校验）
            val tooLong = allChunks.firstOrNull { it.length > maxSequenceLength }
            if (tooLong != null) {
                return Result.failure(SplitException("At least one chunk exceeds maxSequenceLength after splitting"))
            }

            return Result.success(allChunks)
        } catch (e: Exception) {
            return Result.failure(SplitException("Failed to parse/split HTML table: ${e.message}"))
        }
    }

    // ---------- 辅助逻辑：解析表格并展开 rowspan/colspan，返回矩阵与表头行数 ----------
    private data class ParsedTable(val grid: List<List<String>>, val headerRowCount: Int)

    private fun parseTableWithHeaderInfo(table: Element): ParsedTable {
        // 先收集 <tr> 顺序，并记录该行是否包含 <th>（用于识别表头行）
        val rowElements = table.select("thead tr, tbody tr, tfoot tr, tr")
        val hasThFlags = rowElements.map { tr -> tr.select("th").isNotEmpty() } // true 表示该 tr 包含 th

        // 我们需要在构建矩阵时同时保留每一行的单元格内容（考虑 colspan/rowspan）
        val grid = mutableListOf<MutableList<String?>>() // 使用 null 占位，后再转为非空字符串

        var currentRowIndex = 0
        for ((rowIdx, tr) in rowElements.withIndex()) {
            // 确保 grid 有当前行
            ensureGridHasRow(grid, currentRowIndex)

            // 遍历单元格（th 或 td），按出现顺序放到当前行中第一个空位
            val cells = tr.select("th, td")
            var colPointer = 0
            for (cell in cells) {
                // 跳到当前行第一个可用列（null 位置）
                colPointer = findNextAvailableColumn(grid, currentRowIndex, start = colPointer)

                val rawText = cleanCellText(cell)
                val colspan = parseSpan(cell.attr("colspan"))
                val rowspan = parseSpan(cell.attr("rowspan"))

                // 确保 grid 有足够的行以放置 rowspan
                for (r in 0 until rowspan) {
                    ensureGridHasRow(grid, currentRowIndex + r)
                }

                // 确保每受影响的行都有足够的列长度
                for (r in 0 until rowspan) {
                    ensureRowHasCols(grid[currentRowIndex + r], colPointer + colspan)
                }

                // 将 cell 放入左上角，其他被占位的位置置为 ""（占位）
                for (r in 0 until rowspan) {
                    for (c in 0 until colspan) {
                        val targetRow = grid[currentRowIndex + r]
                        val targetCol = colPointer + c
                        // 只有左上角位置放入文本，其它位置放空字符串（以保持矩阵一致性）
                        if (r == 0 && c == 0) {
                            targetRow[targetCol] = rawText
                        } else {
                            // 如果位置已经被占（理论上不应该），我们保持已有值（以防 HTML 非规范）
                            if (targetRow[targetCol] == null) {
                                targetRow[targetCol] = ""
                            }
                        }
                    }
                }

                // 前移列指针
                colPointer += colspan
            }

            // 当前 tr 处理完，下一 tr 使用下一行索引（注意 rowspan 会让后续行部分列被占）
            currentRowIndex++
        }

        // 最后把所有 null 转为 ""，并规整每行的列数（使每行列数一致）
        val maxCols = grid.maxOfOrNull { it.size } ?: 0
        val finalGrid = grid.map { row ->
            // 补齐长度到 maxCols，null -> ""
            val newRow = MutableList(maxCols) { "" }
            for (c in 0 until maxCols) {
                val v = if (c < row.size) row[c] else null
                newRow[c] = v ?: ""
            }
            newRow
        }

        // 计算 headerRowCount：我们把文档开头连续的 hasThFlags==true 行视为 header 行（常见表头场景）
        var headerRowCount = 0
        for (flag in hasThFlags) {
            if (flag) headerRowCount++ else break
        }
        // 若没有 <th> 标记，但前几行视觉上像表头（例如多层表头用 td 也实现），可以不做强制猜测 —— 目前仅识别有 th 的开头行
        return ParsedTable(finalGrid, headerRowCount)
    }

    private fun parseSpan(spanAttr: String?): Int {
        return try {
            val s = spanAttr?.trim()
            if (s.isNullOrEmpty()) 1 else s.toInt().coerceAtLeast(1)
        } catch (e: Exception) {
            1
        }
    }

    private fun ensureGridHasRow(grid: MutableList<MutableList<String?>>, rowIndex: Int) {
        while (grid.size <= rowIndex) {
            grid.add(mutableListOf())
        }
    }

    private fun ensureRowHasCols(row: MutableList<String?>, cols: Int) {
        while (row.size < cols) {
            row.add(null)
        }
    }

    private fun findNextAvailableColumn(grid: MutableList<MutableList<String?>>, rowIndex: Int, start: Int): Int {
        ensureGridHasRow(grid, rowIndex)
        var c = start
        while (true) {
            ensureRowHasCols(grid[rowIndex], c + 1)
            if (grid[rowIndex][c] == null) return c
            c++
        }
    }

    private fun cleanCellText(cell: Element): String {
        // 使用 Jsoup 的 text() 自动处理子标签与实体转义；去首尾空白并压缩空白
        return cell.text()
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

fun main() {
    val text = """
           <table><tr><td></td><td rowspan="2" colspan="1">序号</td><td rowspan="2" colspan="2">水库名称</td><td rowspan="1" colspan="4">水库防洪能力</td><td rowspan="1" colspan="4">大坝情 况</td><td rowspan="1" colspan="4">溢 洪 道</td></tr><tr><td></td><td rowspan="1" colspan="1">集水面积(km{)</td><td rowspan="1" colspan="1">总库容（万方）</td><td rowspan="1" colspan="1">正常水位（米)</td><td rowspan="1" colspan="1">汛期控制水位（米)</td><td rowspan="1" colspan="1">坝型</td><td rowspan="1" colspan="1">坝顶高程(米)</td><td rowspan="1" colspan="1">最大坝高(米)</td><td rowspan="1" colspan="1">坝顶宽度（米)</td><td rowspan="1" colspan="1">形式</td><td rowspan="1" colspan="1">底高程（米）</td><td rowspan="1" colspan="1">底宽（米）</td><td rowspan="1" colspan="1">最大泄洪量(m^{/s)${'$'}</td></tr></table>
    """.trimIndent()
    val result = HtmlTableToCsvSplitter.split(text, 500)
    assert(result.isSuccess) {
        result.exceptionOrNull()?.message!!
    }
    val data = result.getOrThrow()
    for (i in 0..<data.size) {
        println("第${i}个片段：")
        println(data[i])
    }
}