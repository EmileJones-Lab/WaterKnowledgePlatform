package top.emilejones.hhu.textsplitter.service

/**
 * 摘要生成服务接口
 *
 * @author EmileJones
 */
interface ISummarizationService {
    /**
     * 为给定的文本生成摘要
     *
     * @param text 原始文本
     * @return 生成的摘要内容
     */
    fun summarize(text: String): String

    /**
     * 基于上下文信息为给定的文本生成摘要
     *
     * @param text 需要生成摘要的文本
     * @param context 辅助理解文本的上下文信息
     * @return 结合上下文生成的摘要
     */
    fun summarizeWithContext(text: String, context: String): String

    /**
     * 基于子节点的摘要或文本，为父节点生成合并后的摘要
     *
     * @param title 当前节点的标题/文本
     * @param childrenSummaries 子节点的摘要列表
     * @return 合并并精炼后的摘要
     */
    fun summarizeWithChildren(title: String, childrenSummaries: List<String>): String

    /**
     * 为 HTML 格式的表格生成摘要
     *
     * @param tableHtml HTML 格式的表格内容
     * @param prevSegment 上一个片段（可选上下文）
     * @param nextSegment 下一个片段（可选上下文）
     * @return 表格摘要
     */
    fun summarizeTable(tableHtml: String, prevSegment: String? = null, nextSegment: String? = null): String
}
