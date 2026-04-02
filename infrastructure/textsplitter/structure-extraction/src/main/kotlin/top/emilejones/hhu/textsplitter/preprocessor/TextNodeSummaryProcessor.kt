package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
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
        try {
            if (childCount == 0) {
                // 叶子节点：直接对文本生成摘要
                if (nowNode.text.isNotBlank()) {
                    nowNode.summary = summarizationService.summarize(nowNode.text)
                    logger.debug("Generated summary for leaf node seq: [{}]", nowNode.seq)
                }
            } else {
                // 中间节点：收集子节点的摘要或原文
                val childrenContexts = mutableListOf<String>()
                for (i in 0 until childCount) {
                    val child = nowNode.getChild(i)
                    // 优先使用子节点的摘要，如果没有则使用原文
                    val context = child.summary ?: child.text
                    if (context.isNotBlank()) {
                        childrenContexts.add(context)
                    }
                }

                if (childrenContexts.isNotEmpty()) {
                    nowNode.summary = summarizationService.summarizeWithChildren(nowNode.text, childrenContexts)
                    logger.debug("Generated aggregate summary for intermediate node seq: [{}]", nowNode.seq)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to generate summary for node seq: [{}], error: {}", nowNode.seq, e.message)
            // 摘要生成失败不应中断整个流程，保留为 null 即可
        }
    }
}
