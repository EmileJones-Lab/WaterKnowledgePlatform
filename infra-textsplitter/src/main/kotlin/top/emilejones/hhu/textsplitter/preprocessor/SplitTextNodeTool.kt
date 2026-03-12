package top.emilejones.hhu.textsplitter.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.spliter.impl.HtmlTableToCsvSplitter
import top.emilejones.hhu.textsplitter.spliter.impl.PunctuationSplitter
import java.util.*

/**
 * 切分节点的工具
 *
 * ## 作用
 * 在原本的树状结构上，对长度过长的节点进行切割，将其切割为多个节点
 *
 * @author EmileJones
 */
class SplitTextNodeTool(
    private val rootNode: TextNodeDTO,
    private val maxTableLength: Int,
    private val maxSentenceLength: Int
) : Runnable {
    private val logger = LoggerFactory.getLogger(SplitTextNodeTool::class.java)
    private var isOver = false

    override fun run() {
        if (isOver)
            return
        deepVisit(rootNode)
        correctSeq(rootNode)
        isOver = true
    }

    /**
     * 将整个文件树的seq字段重新按照顺序设置
     * @param rootNode 根节点（需要传入最上层的无效节点）
     */
    private fun correctSeq(rootNode: TextNodeDTO) {
        if (rootNode.type != TextType.NULL)
            throw IllegalArgumentException("需要传入最上层的节点")
        var nowNode = rootNode.nextNode
        while (nowNode != null) {
            nowNode.seq = nowNode.preNode!!.seq + 1
            nowNode = nowNode.nextNode
        }
    }

    /**
     * 深度遍历处理所有节点
     * @param nowNode 当前遍历到的节点
     */
    private fun deepVisit(nowNode: TextNodeDTO) {
        // 如果是表格则进行处理
        when (nowNode.type) {
            TextType.TABLE -> handleTableNode(nowNode)
            TextType.COMMON_TEXT -> handleSentence(nowNode)
            else -> Unit
        }

        // 遍历自己的其他孩子
        var index = 0
        while (index < nowNode.childNum()) {
            deepVisit(nowNode.getChild(index))
            index++
        }
    }

    /**
     * 处理表格节点
     * @param tableNode 表格节点
     */
    private fun handleTableNode(tableNode: TextNodeDTO) {
        if (!tableNode.text.startsWith("<table"))
            return
        // 将HTML table转换为Csv
        val splitResults = HtmlTableToCsvSplitter.split(tableNode.text, maxTableLength).getOrThrow()
        val newNodeList = splitResults.map {
            TextNodeDTO(
                text = it,
                seq = -1,
                level = Int.MAX_VALUE,
                type = TextType.TABLE,
                id = UUID.randomUUID().toString()
            )
        }
        rebindRelationship(tableNode, newNodeList)
    }

    /**
     * 处理句子节点
     * @param sentenceNode 句子节点
     */
    private fun handleSentence(sentenceNode: TextNodeDTO) {

        if (sentenceNode.text.length < maxSentenceLength)
            return
        logger.debug(
            "Handling sentence node of file [{}], seq number: [{}]",
            sentenceNode.fileNode!!.id,
            sentenceNode.seq
        )
        // 切分句子
        val splitResults = PunctuationSplitter.split(sentenceNode.text, maxSentenceLength).getOrThrow()
        val newNodeList = splitResults.map {
            if (it.length > maxSentenceLength)
                throw RuntimeException("Find a sentence sequence length [${it.length}] is more than $maxSentenceLength")
            TextNodeDTO(
                text = it,
                seq = -1,
                level = Int.MAX_VALUE,
                type = TextType.TABLE,
                id = UUID.randomUUID().toString()
            )
        }
        rebindRelationship(sentenceNode, newNodeList)
    }

    /**
     * ## 功能
     * 如果旧的节点被拆分成多个节点，那么则会调用这个方法，将旧的节点替换为新的节点
     *
     * ## 注意
     * 1.只是将序列顺序和父子顺序从旧的节点变为新的节点列表，但是seq字段并不正确，需要调用correctSeq将seq字段改正
     * 2.本方法不会鉴定
     *
     * @see correctSeq 重新设置seq字段
     * @param oldNode 被拆分的节点，只能是叶子节点
     * @param newNodeList 被拆分后的节点片段
     */
    private fun rebindRelationship(oldNode: TextNodeDTO, newNodeList: List<TextNodeDTO>) {
        // 记录其他节点
        val preNode = oldNode.preNode
        val nextNode = oldNode.nextNode
        val parentNode = oldNode.parentNode
            ?: throw IllegalArgumentException("此文件没有标题，请检查此文件: [${oldNode.fileNode?.id}]")

        if (oldNode.childNum() != 0)
            throw IllegalStateException("不能切分非叶子节点")

        // 找到当前节点是父亲的第几个孩子
        var index = -1
        for (i in 0..<parentNode.childNum()) {
            if (oldNode != parentNode.getChild(i))
                continue
            index = i
            break
        }

        // 如果节点没有被拆分，则不只做简单的替换
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


        // 重新绑定所有节点的关系
        for (i in 1..<newNodeList.size - 1) {
            newNodeList[i].preNode = newNodeList[i - 1]
            newNodeList[i].nextNode = newNodeList[i + 1]
            newNodeList[i].parentNode = parentNode
            newNodeList[i].fileNode = parentNode.fileNode
        }
        newNodeList[0].preNode = preNode
        newNodeList[0].parentNode = parentNode
        newNodeList[0].fileNode = parentNode.fileNode
        newNodeList[0].nextNode = newNodeList[1]

        newNodeList.last().preNode = newNodeList[newNodeList.size - 2]
        newNodeList.last().parentNode = parentNode
        newNodeList.last().fileNode = parentNode.fileNode
        newNodeList.last().nextNode = nextNode

        preNode?.nextNode = newNodeList[0]
        nextNode?.preNode = newNodeList.last()


        // 将旧的节点删除，将新的节点插入
        parentNode.deleteChild(index)
        for (newNode in newNodeList) {
            parentNode.setChild(newNode, index)
            index++
        }
    }
}

