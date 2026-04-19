package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.service.ISummarizationService

/**
 * 文本节点摘要抽取处理器
 *
 * ## 作用
 * 采用自底向上的方式（后序遍历），逐层为树中的节点生成摘要。
 * 1. 对于叶子节点：直接对节点原文生成摘要。
 * 2. 对于中间节点：基于子节点的摘要列表生成当前层级的摘要。
 * 3. 根节点处理：生成摘要后，将其同步更新到所属的 FileNodeDTO.fileAbstract 中。
 *
 * @author EmileJones
 */
class TextNodeSummaryProcessor(
    private val rootNode: TextNodeDTO,
    private val summarizationService: ISummarizationService
) : Runnable {
    private val logger = LoggerFactory.getLogger(TextNodeSummaryProcessor::class.java)
    private var isProcessed = false

    override fun run() {
        if (isProcessed) return

        logger.info("Starting TextNodeSummaryProcessor for root node, id: [{}]", rootNode.id)
        
        // 1. 递归生成所有节点的摘要
        generateSummaryRecursively(rootNode)
        
        // 2. 将全文摘要同步到 FileNode
        rootNode.fileNode?.let { fileNode ->
            fileNode.fileAbstract = rootNode.summary
            logger.info("Updated FileNode abstract for fileId: [{}]", fileNode.fileId)
        } ?: logger.warn("Root node has no associated FileNode, cannot update file abstract.")

        isProcessed = true
        logger.info("TextNodeSummaryProcessor execution completed.")
    }

    /**
     * 递归为节点生成摘要（后序遍历）
     */
    private fun generateSummaryRecursively(nowNode: TextNodeDTO) {
        val childCount = nowNode.childNum()

        // 1. 先递归处理所有子节点
        for (i in 0 until childCount) {
            generateSummaryRecursively(nowNode.getChild(i))
        }

        // 2. 处理当前节点摘要
        processNodeSummary(nowNode)
    }

    /**
     * 根据节点类型和子节点情况，分发执行具体的摘要生成逻辑
     */
    private fun processNodeSummary(nowNode: TextNodeDTO) {
        try {
            when {
                nowNode.type == TextType.TABLE -> summarizeTableNode(nowNode)
                nowNode.childNum() == 0 -> summarizeLeafNode(nowNode)
                else -> summarizeIntermediateNode(nowNode)
            }
        } catch (e: Exception) {
            logger.error("Failed to generate summary for node seq: [{}], error: {}", nowNode.seq, e.message)
            // 摘要生成失败不应中断整个流程，保留为 null 即可
        }
    }

    /**
     * 处理表格节点的专用摘要逻辑
     */
    private fun summarizeTableNode(node: TextNodeDTO) {
        if (node.text.isNotBlank()) {
            node.summary = summarizationService.summarizeTable(
                node.text,
                node.preNode?.text,
                node.nextNode?.text
            )
            logger.debug("Generated table summary for node seq: [{}]", node.seq)
        }
    }

    /**
     * 处理叶子节点的摘要逻辑
     */
    private fun summarizeLeafNode(node: TextNodeDTO) {
        if (node.text.isNotBlank()) {
            node.summary = summarizationService.summarize(node.text)
            logger.debug("Generated summary for leaf node seq: [{}]", node.seq)
        }
    }

    /**
     * 处理中间节点的聚合摘要逻辑
     */
    private fun summarizeIntermediateNode(node: TextNodeDTO) {
        val childCount = node.childNum()
        val childrenContexts = mutableListOf<String>()

        for (i in 0 until childCount) {
            val child = node.getChild(i)
            // 优先使用子节点的摘要，如果没有则使用原文
            val context = child.summary ?: child.text
            if (context.isNotBlank()) {
                childrenContexts.add(context)
            }
        }

        if (childrenContexts.isNotEmpty()) {
            node.summary = summarizationService.summarizeWithChildren(node.text, childrenContexts)
            logger.debug("Generated aggregate summary for intermediate node seq: [{}]", node.seq)
        }
    }
}
