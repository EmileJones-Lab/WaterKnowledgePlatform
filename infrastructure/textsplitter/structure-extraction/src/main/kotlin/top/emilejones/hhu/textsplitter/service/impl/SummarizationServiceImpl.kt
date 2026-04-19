package top.emilejones.hhu.textsplitter.service.impl

import org.springframework.stereotype.Service
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.service.ISummarizationService

/**
 * 摘要生成服务实现类
 *
 * @author EmileJones
 */
@Service
class SummarizationServiceImpl(
    private val modelClient: ModelClient
) : ISummarizationService {

    companion object {
        private const val SYSTEM_PROMPT = "你是一个专业的文档处理助手，擅长对各种文本内容进行精准、客观的摘要提取。你的任务是帮助用户从复杂的文档结构中提取核心信息。"
        
        private const val LEAF_SUMMARIZE_PROMPT = """
            请为以下文本生成一段简短的摘要（不超过 100 字）。
            要求：
            1. 保持客观，不要加入个人评论。
            2. 突出文本中的核心概念、核心事实或关键结论。
            3. 如果文本很短，请直接保留其核心含义。
            
            待处理文本：
            ---
            %s
            ---
            摘要内容：
        """

        private const val TABLE_SUMMARIZE_PROMPT = """
            请为以下 HTML 格式的表格生成一段准确的摘要（不超过 150 字）。
            
            上下文参考：
            - 上一个片段：[%s]
            - 下一个片段：[%s]
            
            待处理表格内容：
            ---
            %s
            ---
            
            要求：
            1. 概括表格的主题、核心数据维度或关键对比关系。
            2. 结合上下文，说明表格在文档中的作用或其反映的核心事实。
            3. 摘要应当通顺、精炼。
            
            表格摘要：
        """

        private const val AGGREGATE_SUMMARIZE_PROMPT = """
            当前章节标题为：[%s]
            
            以下是该章节下所有子内容的摘要列表：
            ---
            %s
            ---
            
            请根据上述子内容摘要，为本章节生成一个整体的合并摘要（不超过 200 字）。
            要求：
            1. 梳理各子内容之间的逻辑关系。
            2. 概括本章节在全文中承载的核心信息。
            3. 摘要应当通顺、精炼，具有统领性。
            
            章节合并摘要：
        """
    }

    override fun summarize(text: String): String {
        if (text.isBlank()) return ""
        
        // 如果文本已经非常短，可能不需要摘要，或者直接返回
        if (text.length < 50) return text.trim()

        val userPrompt = LEAF_SUMMARIZE_PROMPT.format(text)
        return modelClient.llm(SYSTEM_PROMPT, userPrompt).trim()
    }

    override fun summarizeWithChildren(title: String, childrenSummaries: List<String>): String {
        if (childrenSummaries.isEmpty()) return summarize(title)

        val childrenContext = childrenSummaries.joinToString("\n") { "- $it" }
        val userPrompt = AGGREGATE_SUMMARIZE_PROMPT.format(title.ifBlank { "未命名章节" }, childrenContext)
        
        return modelClient.llm(SYSTEM_PROMPT, userPrompt).trim()
    }

    override fun summarizeTable(tableHtml: String, prevSegment: String?, nextSegment: String?): String {
        if (tableHtml.isBlank()) return ""

        val userPrompt = TABLE_SUMMARIZE_PROMPT.format(
            prevSegment ?: "无",
            nextSegment ?: "无",
            tableHtml
        )

        return modelClient.llm(SYSTEM_PROMPT, userPrompt).trim()
    }
}
