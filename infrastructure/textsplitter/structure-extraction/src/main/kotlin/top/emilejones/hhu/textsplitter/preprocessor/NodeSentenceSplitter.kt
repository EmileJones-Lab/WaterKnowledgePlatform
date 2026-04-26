package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import java.util.*

/**
 * COMMON_TEXT 节点句子切割器
 *
 * ## 作用
 * 深度遍历树结构，找到所有类型为 COMMON_TEXT 的叶子节点，
 * 按照句子结束符将其切割成多个节点，确保每个节点只包含一句完整的话。
 *
 * ## 切割规则
 * 1. 只处理类型为 COMMON_TEXT 的叶子节点（childNum == 0）。
 * 2. 按照配置的句子结束符（默认："。", ";", "；", "?", "？", "!", "！"）进行切割。
 * 3. 切割后保留句子结束符在每个片段的末尾。
 * 4. 将原节点替换为切割后的多个节点，重新绑定父子关系和前后关系。
 * 5. 最后重新校正整棵树的 seq 字段。
 *
 * ## 新节点属性
 * - type: 保持 COMMON_TEXT
 * - level: 保持 Int.MAX_VALUE
 * - seq: 由 correctSeq() 方法重新校正
 *
 * @param rootNode 根节点
 * @param sentenceDelimiters 句子结束符列表，默认为常见中文/英文句子结束符
 * @author EmileJones
 */
class NodeSentenceSplitter(
    private val rootNode: TextNodeDTO,
    private val sentenceDelimiters: List<String> = listOf("。", ";", "；", "?", "？", "!", "！")
) : Runnable {
    private val logger = LoggerFactory.getLogger(NodeSentenceSplitter::class.java)
    private var isProcessed = false

    override fun run() {
        if (isProcessed) return

        logger.info("Starting CommonTextNodeSplitter for root node, seq: [{}]", rootNode.seq)
        deepVisit(rootNode)
        correctSeq(rootNode)
        isProcessed = true
        logger.info("CommonTextNodeSplitter execution completed.")
    }

    /**
     * 深度遍历处理所有节点（后序遍历）
     *
     * @param nowNode 当前遍历到的节点
     */
    private fun deepVisit(nowNode: TextNodeDTO) {
        val childCount = nowNode.childNum()

        // 1. 先递归处理子节点
        var index = 0
        while (index < childCount) {
            deepVisit(nowNode.getChild(index))
            index++
        }

        // 2. 处理当前节点：只处理 COMMON_TEXT 类型的叶子节点
        if (nowNode.type == TextType.COMMON_TEXT && nowNode.childNum() == 0) {
            splitNode(nowNode)
        }
    }

    /**
     * 对单个 COMMON_TEXT 叶子节点进行句子切割
     *
     * @param node 需要切割的叶子节点
     */
    private fun splitNode(node: TextNodeDTO) {
        val originalText = node.text
        if (originalText.isBlank()) return

        // 切割句子
        val sentences = splitIntoSentences(originalText)
        if (sentences.size <= 1) return

        logger.debug(
            "Splitting COMMON_TEXT node seq: [{}] into [{}] sentences",
            node.seq, sentences.size
        )

        // 创建新的节点列表
        val newNodeList = sentences.map { sentence ->
            TextNodeDTO(
                text = sentence,
                seq = -1,
                level = Int.MAX_VALUE,
                type = TextType.COMMON_TEXT,
                id = UUID.randomUUID().toString()
            )
        }

        // 重新绑定节点关系
        rebindRelationship(node, newNodeList)
    }

    /**
     * 将文本按照句子结束符切割成多个句子
     *
     * ## 规则
     * 1. 引号（中文/英文双引号）内的内容视为一个整体，不按照句子结束符拆分。
     * 2. 引号外的文本按照句子结束符切割。
     * 3. 保留结束符在每个句子的末尾。
     * 4. 过滤掉空白的片段。
     * 5. 如果文本末尾没有结束符，最后一段也会被保留。
     *
     * @param text 需要切割的文本
     * @return 切割后的句子列表
     */
    private fun splitIntoSentences(text: String): List<String> {
        val result = mutableListOf<String>()
        val currentSentence = StringBuilder()

        // 引号栈：用于跟踪嵌套引号
        val quoteStack = ArrayDeque<Char>()

        var i = 0
        while (i < text.length) {
            val ch = text[i]

            // 处理引号：中文双引号、英文双引号
            when (ch) {
                '\u201C' -> quoteStack.addLast('\u201C')           // 中文左双引号 "
                '\u201D' -> {
                    if (quoteStack.isNotEmpty() && quoteStack.last() == '\u201C') {
                        quoteStack.removeLast()                     // 中文右双引号 "
                    }
                }
                '"' -> {
                    if (quoteStack.isNotEmpty() && quoteStack.last() == '"') {
                        quoteStack.removeLast()                     // 英文双引号
                    } else {
                        quoteStack.addLast('"')
                    }
                }
            }

            currentSentence.append(ch)

            // 只有在不在引号内时，才检查句子结束符
            if (quoteStack.isEmpty()) {
                val matchedDelimiter = sentenceDelimiters.find { text.startsWith(it, i) }
                if (matchedDelimiter != null) {
                    val trimmed = currentSentence.toString().trim()
                    if (trimmed.isNotEmpty()) {
                        result.add(trimmed)
                    }
                    currentSentence.clear()
                    i += matchedDelimiter.length
                    continue
                }
            }

            i++
        }

        // 处理最后未完成的句子
        if (currentSentence.isNotEmpty()) {
            val trimmed = currentSentence.toString().trim()
            if (trimmed.isNotEmpty()) {
                result.add(trimmed)
            }
        }

        return result
    }

    /**
     * 将旧节点替换为新的节点列表，重新绑定所有关系
     *
     * @param oldNode 被替换的旧节点（必须是叶子节点）
     * @param newNodeList 新的节点列表
     */
    private fun rebindRelationship(oldNode: TextNodeDTO, newNodeList: List<TextNodeDTO>) {
        val preNode = oldNode.preNode
        val nextNode = oldNode.nextNode
        val parentNode = oldNode.parentNode
            ?: throw IllegalArgumentException("Cannot split node without parent: [${oldNode.fileNode?.id}]")

        if (oldNode.childNum() != 0) {
            throw IllegalStateException("Cannot split non-leaf node, seq: [${oldNode.seq}]")
        }

        // 找到当前节点在父节点中的位置
        var index = -1
        for (i in 0 until parentNode.childNum()) {
            if (oldNode == parentNode.getChild(i)) {
                index = i
                break
            }
        }
        if (index == -1) {
            throw IllegalStateException("Node not found in parent's children, seq: [${oldNode.seq}]")
        }

        // 如果只有一个新节点，做简单替换
        if (newNodeList.size == 1) {
            val newNode = newNodeList.first()
            newNode.parentNode = parentNode
            newNode.preNode = preNode
            newNode.nextNode = nextNode
            newNode.fileNode = oldNode.fileNode

            nextNode?.preNode = newNode
            preNode?.nextNode = newNode
            parentNode.deleteChild(index)
            parentNode.setChild(newNode, index)
            return
        }

        // 重新绑定中间节点的关系
        for (i in 1 until newNodeList.size - 1) {
            newNodeList[i].preNode = newNodeList[i - 1]
            newNodeList[i].nextNode = newNodeList[i + 1]
            newNodeList[i].parentNode = parentNode
            newNodeList[i].fileNode = oldNode.fileNode
        }

        // 第一个节点
        newNodeList[0].preNode = preNode
        newNodeList[0].parentNode = parentNode
        newNodeList[0].fileNode = oldNode.fileNode
        newNodeList[0].nextNode = newNodeList[1]

        // 最后一个节点
        val lastIndex = newNodeList.size - 1
        newNodeList[lastIndex].preNode = newNodeList[lastIndex - 1]
        newNodeList[lastIndex].parentNode = parentNode
        newNodeList[lastIndex].fileNode = oldNode.fileNode
        newNodeList[lastIndex].nextNode = nextNode

        // 更新前后节点的指针
        preNode?.nextNode = newNodeList[0]
        nextNode?.preNode = newNodeList[lastIndex]

        // 删除旧节点，插入新节点
        parentNode.deleteChild(index)
        for (newNode in newNodeList) {
            parentNode.setChild(newNode, index)
            index++
        }
    }

    /**
     * 重新校正整棵树的 seq 字段
     *
     * @param rootNode 根节点（必须是最上层的 NULL 类型节点）
     */
    private fun correctSeq(rootNode: TextNodeDTO) {
        if (rootNode.type != TextType.NULL) {
            throw IllegalArgumentException("Need to pass the top-level NULL node for seq correction")
        }
        var nowNode = rootNode.nextNode
        while (nowNode != null) {
            nowNode.seq = nowNode.preNode!!.seq + 1
            nowNode = nowNode.nextNode
        }
    }
}
