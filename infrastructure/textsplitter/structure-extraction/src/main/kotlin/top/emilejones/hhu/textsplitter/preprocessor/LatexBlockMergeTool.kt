package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import java.util.*

/**
 * 处理 Latex 块节点的合并工具。
 *
 * Markdown 中的 Latex 块通常由 "$$" 包裹，且由于换行切割，这些块可能被拆分为多个节点。
 * 本工具扫描节点序列，将由 "$$" 包裹的多行内容合并为一个节点，并将其类型设置为 [TextType.LATEX]。
 *
 * @author Gemini CLI
 */
class LatexBlockMergeTool(
    private val rootNode: TextNodeDTO
) : Runnable {
    private val logger = LoggerFactory.getLogger(LatexBlockMergeTool::class.java)
    private val delimiter = "$$"

    override fun run() {
        validateRootNode()

        var nowNode = rootNode.nextNode
        while (nowNode != null) {
            // 尝试查找以当前节点开始的 Latex 块
            val latexNodes = findLatexBlock(nowNode)
            
            if (latexNodes != null) {
                // 找到完整的 Latex 块，执行合并
                val mergedNode = mergeNodes(latexNodes)
                // 继续从合并后的节点的下一个节点开始扫描
                nowNode = mergedNode.nextNode
            } else {
                // 未找到完整的 Latex 块，继续向下扫描
                nowNode = nowNode.nextNode
            }
        }

        // 重新计算并设置所有节点的 seq 字段
        correctSeq(rootNode)
    }

    private fun validateRootNode() {
        if (rootNode.type != TextType.NULL) {
            throw IllegalArgumentException("需要传入根节点 (NULL 类型)")
        }
    }

    /**
     * 从起始节点开始，尝试寻找被 "$$" 包裹的 Latex 块节点序列。
     * 如果找到了完整的序列（包含起始和结束分隔符），则返回该序列。
     */
    private fun findLatexBlock(startNode: TextNodeDTO): List<TextNodeDTO>? {
        if (startNode.text.trim() != delimiter) {
            return null
        }

        val nodesToMerge = mutableListOf<TextNodeDTO>()
        nodesToMerge.add(startNode)

        var scanNode = startNode.nextNode
        while (scanNode != null) {
            nodesToMerge.add(scanNode)
            if (scanNode.text.trim() == delimiter) {
                // 找到了结束标志
                // 如果块内至少包含三个节点（$$，内容，$$），或者虽然只有两个节点但内容完整，则认为是一个块
                // 这里要求至少两个节点（两个 $$ 节点），以防单行内已经包含了结束符的情况（虽然按行切分通常会有两个 $$ 节点）
                return if (nodesToMerge.size >= 2) nodesToMerge else null
            }
            scanNode = scanNode.nextNode
        }
        
        return null // 未找到结束标志
    }

    /**
     * 将一组节点合并为一个新的 [TextType.LATEX] 节点，并更新树状结构。
     */
    private fun mergeNodes(nodes: List<TextNodeDTO>): TextNodeDTO {
        val mergedNode = createMergedNode(nodes)
        
        updateSequencePointers(nodes, mergedNode)
        updateParentStructure(nodes, mergedNode)

        logger.debug("已将 {} 个节点合并为一个 Latex 块节点", nodes.size)
        return mergedNode
    }

    /**
     * 根据节点序列创建合并后的新 Latex 节点。
     */
    private fun createMergedNode(nodes: List<TextNodeDTO>): TextNodeDTO {
        val firstNode = nodes.first()
        val combinedText = nodes.joinToString("\n") { it.text }

        return TextNodeDTO(
            id = UUID.randomUUID().toString(),
            text = combinedText,
            seq = firstNode.seq,
            level = firstNode.level,
            type = TextType.LATEX
        ).apply {
            this.fileNode = firstNode.fileNode
            this.parentNode = firstNode.parentNode
        }
    }

    /**
     * 更新全局序列中的前后驱指针 (preNode/nextNode)。
     */
    private fun updateSequencePointers(oldNodes: List<TextNodeDTO>, newNode: TextNodeDTO) {
        val firstNode = oldNodes.first()
        val lastNode = oldNodes.last()
        
        val preNode = firstNode.preNode
        val nextNode = lastNode.nextNode

        newNode.preNode = preNode
        newNode.nextNode = nextNode

        preNode?.nextNode = newNode
        nextNode?.preNode = newNode
    }

    /**
     * 更新父节点的子节点列表 (childList)。
     */
    private fun updateParentStructure(oldNodes: List<TextNodeDTO>, newNode: TextNodeDTO) {
        val parentNode = oldNodes.first().parentNode ?: return
        val firstNode = oldNodes.first()

        // 1. 找到起始节点在父节点中的索引
        var indexInParent = -1
        for (i in 0 until parentNode.childNum()) {
            if (parentNode.getChild(i) === firstNode) {
                indexInParent = i
                break
            }
        }

        if (indexInParent == -1) {
            throw IllegalStateException("未在父节点的子节点列表中找到起始节点 [${firstNode.id}]")
        }

        // 2. 从父节点中删除所有旧节点，并插入新节点
        repeat(oldNodes.size) {
            parentNode.deleteChild(indexInParent)
        }
        parentNode.setChild(newNode, indexInParent)
    }

    /**
     * 纠正树中所有节点的序列号。
     */
    private fun correctSeq(rootNode: TextNodeDTO) {
        var nowNode = rootNode.nextNode
        var currentSeq = 0
        while (nowNode != null) {
            nowNode.seq = currentSeq++
            nowNode = nowNode.nextNode
        }
    }
}
