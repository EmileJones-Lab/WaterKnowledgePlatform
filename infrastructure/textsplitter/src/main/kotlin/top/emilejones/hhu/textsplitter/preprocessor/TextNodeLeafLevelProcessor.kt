package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO
import top.emilejones.hhu.domain.result.TextType

/**
 * 叶子节点层级处理器
 *
 * ## 作用
 * 深度遍历树结构，如果某个节点的所有子节点（即该节点的所有孩子）都是叶子节点，
 * 则统一处理这些子节点：
 * 1. 将 level 设置为 Int.MAX_VALUE（若原先不是）。
 * 2. 如果 type 为 COMMON_TEXT，则将其改为 TITLE。
 * 3. 移除文本开头的 Markdown 标题符号 "#"。
 *
 * @author EmileJones
 */
class TextNodeLeafLevelProcessor(
    private val rootNode: TextNodeDTO
) : Runnable {
    private val logger = LoggerFactory.getLogger(TextNodeLeafLevelProcessor::class.java)
    private var isProcessed = false

    override fun run() {
        if (isProcessed) return

        logger.info("Starting TextNodeLeafLevelProcessor for root node, seq: [{}]", rootNode.seq)
        deepVisit(rootNode)
        isProcessed = true
        logger.info("TextNodeLeafLevelProcessor execution completed.")
    }

    /**
     * 深度遍历处理所有节点
     * 采用后序遍历逻辑，确保从叶子节点向上处理
     *
     * @param nowNode 当前遍历到的节点
     */
    private fun deepVisit(nowNode: TextNodeDTO) {
        val childCount = nowNode.childNum()
        if (childCount == 0) return

        // 1. 先递归遍历处理子节点
        var index = 0
        while (index < childCount) {
            val child = nowNode.getChild(index)
            deepVisit(child)
            index++
        }

        // 2. 核心逻辑：检查是否所有孩子都是叶子节点
        var allChildrenAreLeaves = true

        for (i in 0 until childCount) {
            val child = nowNode.getChild(i)
            if (child.childNum() != 0) {
                allChildrenAreLeaves = false
                break
            }
        }

        // 3. 如果所有孩子都是叶子节点，执行更新
        if (allChildrenAreLeaves) {
            for (i in 0 until childCount) {
                val child = nowNode.getChild(i)

                // 只有在 level 不是 Int.MAX_VALUE 时才处理
                if (child.level != Int.MAX_VALUE) {
                    child.level = Int.MAX_VALUE

                    // 修改类型：从 COMMON_TEXT 变为 TITLE
                    if (child.type == TextType.COMMON_TEXT) {
                        child.type = TextType.TITLE
                    }

                    // 处理文本：移除开头的所有 "#"
                    val originalText = child.text
                    if (!originalText.isNullOrEmpty() && originalText.startsWith("#")) {
                        child.text = originalText.replace("^#+".toRegex(), "")
                        logger.debug("Processed node seq: [{}], set type to TITLE and removed leading #", child.seq)
                    }
                }
            }
        }
    }
}
