package top.emilejones.hhu.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.platform.KnowledgeApplicationServiceV2
import top.emilejones.hhu.application.platform.RecallApplicationService

@Component
class RetrieveCommand(
    private val knowledgeApplicationService: KnowledgeApplicationServiceV2,
    private val recallApplicationService: RecallApplicationService
) : CliktCommand(name = "retrieve") {

    private val question by argument(help = "The question to ask")
    private val kbIdArg by argument(help = "The knowledge base ID (optional)").optional()

    override fun help(context: Context): String = "Retrieve information from knowledge base"

    override fun run() {
        // 1. 确定目标知识库ID
        // 如果用户在命令行参数中提供了ID，则直接使用；否则让用户进行交互式选择
        val targetKbId = resolveKnowledgeBaseId(kbIdArg)

        // 如果没有获取到有效的ID（例如用户取消选择或无可用库），则直接提示用户相关信息
        if (targetKbId == null) {
            echo("Operation aborted: No valid knowledge base ID was provided or selected.")
            return
        }

        // 2. 执行召回并展示结果
        performRecall(targetKbId, question)
    }

    /**
     * 解析知识库ID。
     * 如果参数中已提供ID，直接返回。
     * 否则，调用交互式选择方法。
     *
     * @param providedId 命令行参数传入的ID（可能为空）
     * @return 选定的知识库ID，如果未选定或无效则返回null
     */
    private fun resolveKnowledgeBaseId(providedId: String?): String? {
        if (providedId != null) {
            return providedId
        }
        return selectKnowledgeBaseInteractively()
    }

    /**
     * 交互式选择知识库。
     * 列出所有结构化的知识库供用户选择。
     *
     * @return 用户选择的知识库ID，或者null
     */
    private fun selectKnowledgeBaseInteractively(): String? {
        // 获取所有结构化知识库列表
        val directories = knowledgeApplicationService.structuredKnowledgeDirectories
        if (directories.isEmpty()) {
            echo("No structured knowledge directories found.")
            return null
        }

        // 打印知识库列表
        echo("Available Knowledge Bases:")
        directories.forEachIndexed { index, dir ->
            echo("${index + 1}. ${dir.kbName} (ID: ${dir.id})")
        }

        // 提示用户输入选择
        echo("Please enter the number of the knowledge base to use:")
        val input = readlnOrNull()
        if (input.isNullOrBlank()) {
            echo("No input provided.")
            return null
        }

        // 解析用户输入并返回对应的ID
        val index = input.toIntOrNull()
        if (index != null && index > 0 && index <= directories.size) {
            return directories[index - 1].id
        }

        echo("Invalid selection.")
        return null
    }

    /**
     * 执行召回操作并打印结果。
     *
     * @param kbId 知识库ID
     * @param query 查询问题
     */
    private fun performRecall(kbId: String, query: String) {
        echo("Recalling from KB ID: $kbId with question: $query")
        try {
            // 调用应用服务进行文本召回
            val results = recallApplicationService.recallText(query, kbId)

            // 展示召回结果
            if (results.isEmpty()) {
                echo("No related content found.")
            } else {
                echo("Found ${results.size} related segments:")
                results.forEachIndexed { i, text ->
                    echo("--- Segment ${i + 1} ---")
                    echo(text)
                    echo("")
                }
            }
        } catch (e: Exception) {
            echo("Error recalling text: ${e.message}")
        }
    }
}