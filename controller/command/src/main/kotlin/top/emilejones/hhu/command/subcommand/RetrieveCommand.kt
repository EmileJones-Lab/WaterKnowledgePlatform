package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.RecallApplicationService
import top.emilejones.hhu.application.command.dto.FileNodeDTO
import top.emilejones.hhu.application.command.dto.TextNodeDTO
import top.emilejones.hhu.command.util.progress.Spinner

/**
 * 召回命令。
 * 用户通过输入问题，从向量数据库中检索相关的知识片段。
 */
@Component
class RetrieveCommand(
    private val recallApplicationService: RecallApplicationService
) : CliktCommand(name = "retrieve") {

    private val query by argument(help = "要查询的问题")
    private val verbose by option("-v", "--verbose", help = "详细模式，展示更多元数据").flag()
    private val showFile by option("-f", "--file", help = "展示召回的相关文件信息").flag()

    override fun help(context: Context): String = "根据问题召回相关的知识片段"

    override fun run() {
        val spinner = Spinner("正在检索与 \"$query\" 相关的内容...")
        spinner.start()

        try {
            // 1. 文件召回
            val files = if (showFile) {
                recallApplicationService.recallFileNodes(query)
            } else null

            // 2. 文本召回（verbose 模式）
            val nodes = if (verbose) {
                recallApplicationService.recallTextNodes(query)
            } else null

            // 3. 文本召回（普通模式）
            val results = if (!verbose) {
                recallApplicationService.recallText(query)
            } else null

            spinner.stop()

            // 4. 展示文件召回结果
            files?.let { displayFileResults(it) }

            // 5. 展示文本召回结果
            nodes?.let { displayNodeResults(it) }
            results?.let { displayTextResults(it) }

        } catch (e: Exception) {
            spinner.stop("检索失败")
            throw e
        }
    }

    private fun displayFileResults(files: List<FileNodeDTO>) {
        if (files.isEmpty()) {
            echo("[文件] 未找到相关文件。")
            return
        }
        echo("[文件] 共召回 ${files.size} 个相关文件：")
        files.forEachIndexed { index, file ->
            echo("  ${index + 1}. 文件名: ${file.fileName} | ID: ${file.id} | 源文档ID: ${file.sourceDocumentId}")
            file.fileAbstract?.let { echo("     摘要: $it") }
        }
        echo("")
    }

    private fun displayNodeResults(nodes: List<TextNodeDTO>) {
        if (nodes.isEmpty()) {
            echo("未找到相关内容。")
            return
        }
        echo("共找到 ${nodes.size} 条文本片段：")
        nodes.forEachIndexed { index, node ->
            echo("--- 结果 ${index + 1} ---")
            echo("ID      : ${node.id}")
            echo("文件名  : ${node.fileName}")
            echo("文件节点: ${node.fileNodeId}")
            echo("序列号  : ${node.seq}")
            echo("层级    : ${node.level}")
            echo("类型    : ${node.type}")
            echo("摘要    : ${node.summary ?: "无"}")
            echo("文本    :")
            echo(node.text)
            echo("")
        }
    }

    private fun displayTextResults(results: List<String>) {
        if (results.isEmpty()) {
            echo("未找到相关内容。")
            return
        }
        echo("共找到 ${results.size} 条文本片段：")
        results.forEachIndexed { index, text ->
            echo("--- 结果 ${index + 1} ---")
            echo(text)
            echo("")
        }
    }
}
