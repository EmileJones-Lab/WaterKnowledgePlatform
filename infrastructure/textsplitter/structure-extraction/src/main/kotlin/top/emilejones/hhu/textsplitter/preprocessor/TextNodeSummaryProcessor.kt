package top.emilejones.hhu.textsplitter.preprocessor

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.service.ISummarizationService

/**
 * 文本节点摘要抽取处理器（并发版）
 *
 * ## 作用
 * 采用自底向上的方式（后序遍历），并行为树中的节点生成摘要。
 * 1. 子节点并行处理：所有子节点的摘要生成任务并发启动。
 * 2. 依赖顺序保证：父节点会等待所有子节点任务完成后，再开始自身的摘要生成。
 * 3. IO 密集型优化：使用 Dispatchers.IO 调度，提高并发吞吐量。
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

        runBlocking {
            logger.info("Starting concurrent TextNodeSummaryProcessor for root node, id: [{}]", rootNode.id)

            // 1. 发起挂起函数进行递归并发处理
            generateSummaryRecursively(rootNode)

            // 2. 将全文摘要同步到 FileNode
            rootNode.fileNode?.let { fileNode ->
                fileNode.fileAbstract = rootNode.summary
                logger.info("Updated FileNode abstract for fileId: [{}]", fileNode.fileId)
            } ?: logger.warn("Root node has no associated FileNode, cannot update file abstract.")

            isProcessed = true
            logger.info("TextNodeSummaryProcessor execution completed.")
        }
    }

    /**
     * 递归并发为节点生成摘要
     */
    private suspend fun generateSummaryRecursively(nowNode: TextNodeDTO) {
        val childCount = nowNode.childNum()

        // 1. 并发启动所有子节点的处理任务
        // coroutineScope 会挂起当前协程，直到内部启动的所有子协程（launch）全部执行完毕
        coroutineScope {
            for (i in 0 until childCount) {
                launch {
                    generateSummaryRecursively(nowNode.getChild(i))
                }
            }
        }

        // 2. 此时所有子节点已确保生成了摘要，开始处理当前节点
        withContext(Dispatchers.IO) {
            processNodeSummary(nowNode)
        }
    }

    /**
     * 根据节点类型分发执行具体的摘要生成逻辑
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
        }
    }

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

    private fun summarizeLeafNode(node: TextNodeDTO) {
        if (node.text.isNotBlank()) {
            node.summary = summarizationService.summarize(node.text)
            logger.debug("Generated summary for leaf node seq: [{}]", node.seq)
        }
    }

    private fun summarizeIntermediateNode(node: TextNodeDTO) {
        val childCount = node.childNum()
        val childrenContexts = (0 until childCount).mapNotNull { i ->
            val child = node.getChild(i)
            // 此时 child.summary 已经由子协程确保生成
            val context = child.summary ?: child.text
            if (context.isNotBlank()) context else null
        }

        if (childrenContexts.isNotEmpty()) {
            node.summary = summarizationService.summarizeWithChildren(node.text, childrenContexts)
            logger.debug("Generated aggregate summary for intermediate node seq: [{}]", node.seq)
        }
    }
}
