package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.RecallApplicationService

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

    override fun help(context: Context): String = "根据问题召回相关的知识片段"

    override fun run() {
        echo("正在检索与 \"$query\" 相关的内容...")

        if (verbose) {
            val nodes = recallApplicationService.recallTextNodes(query)
            if (nodes.isEmpty()) {
                echo("未找到相关内容。")
            } else {
                echo("共找到 ${nodes.size} 条相关内容：")
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
        } else {
            val results = recallApplicationService.recallText(query)

            if (results.isEmpty()) {
                echo("未找到相关内容。")
            } else {
                echo("共找到 ${results.size} 条相关内容：")
                results.forEachIndexed { index, text ->
                    echo("--- 结果 ${index + 1} ---")
                    echo(text)
                    echo("")
                }
            }
        }
    }
}