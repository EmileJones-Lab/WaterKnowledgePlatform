package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
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

    override fun help(context: Context): String = "根据问题召回相关的知识片段"

    override fun run() {
        echo("正在检索与 \"$query\" 相关的内容...")

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