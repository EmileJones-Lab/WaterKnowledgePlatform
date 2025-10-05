package top.emilejones.hhu.preprocessor

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.enums.TextType
import top.emilejones.hhu.spliter.impl.HtmlTableToCsvSplitter
import top.emilejones.hhu.spliter.impl.PunctuationSplitter

class SplitTextNodeTool(
    private val rootNode: TextNode,
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

    private fun correctSeq(rootNode: TextNode) {
        var nowNode = rootNode.nextNode
        while (nowNode != null) {
            nowNode.seq = nowNode.preNode!!.seq + 1
            nowNode = nowNode.nextNode
        }
    }

    private fun deepVisit(nowNode: TextNode) {
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

    private fun handleTableNode(tableNode: TextNode) {
        if (!tableNode.text.startsWith("<table"))
            return
        logger.debug(
            "Handling table node of file [{}], sequence number: [{}]",
            tableNode.fileNode!!.fileName,
            tableNode.seq
        )
        // 记录其他节点
        val preNode = tableNode.preNode
        val nextNode = tableNode.nextNode
        val parentNode = tableNode.parentNode

        // 找到当前节点是父亲的第几个孩子
        var index = -1
        for (i in 0..<parentNode!!.childNum()) {
            if (tableNode != parentNode.getChild(i))
                continue
            index = i
            break
        }

        // 将HTML table转换为Csv
        val splitResults = HtmlTableToCsvSplitter.split(tableNode.text, maxTableLength).getOrThrow()
        val newNodeList = splitResults.map {
            TextNode(
                text = it,
                seq = -1,
                level = Int.MAX_VALUE,
                type = TextType.TABLE
            )
        }
        // 将新创建的节点绑定关系
        if (newNodeList.size == 1) {
            newNodeList[0].preNode = preNode
            newNodeList[0].parentNode = parentNode
            newNodeList[0].fileNode = parentNode.fileNode
            newNodeList[0].nextNode = nextNode
            if (preNode != null)
                preNode.nextNode = newNodeList[0]
            if (nextNode != null)
                nextNode.preNode = newNodeList[0]
        } else {
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

            if (preNode != null)
                preNode.nextNode = newNodeList[0]
            if (nextNode != null)
                nextNode.preNode = newNodeList.last()
        }


        // 将旧的节点删除，将新的节点插入
        parentNode.deleteChild(index)
        for (newNode in newNodeList) {
            parentNode.setChild(newNode, index)
            index++
        }
    }

    private fun handleSentence(sentenceNode: TextNode) {
        if (sentenceNode.text.length < maxSentenceLength)
            return
        // 记录其他节点
        val preNode = sentenceNode.preNode
        val nextNode = sentenceNode.nextNode
        val parentNode = sentenceNode.parentNode

        // 找到当前节点是父亲的第几个孩子
        var index = -1
        for (i in 0..<parentNode!!.childNum()) {
            if (sentenceNode != parentNode.getChild(i))
                continue
            index = i
            break
        }

        // 切分句子
        val splitResults = PunctuationSplitter.split(sentenceNode.text, maxSentenceLength).getOrThrow()
        val newNodeList = splitResults.map {
            if (it.length > maxTableLength)
                throw RuntimeException("Find a yaml sequence length [${it.length}] is more than $maxTableLength")
            TextNode(
                text = it,
                seq = -1,
                level = Int.MAX_VALUE,
                type = TextType.TABLE
            )
        }
        // 将新创建的节点绑定关系
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

        newNodeList[newNodeList.size - 1].preNode = newNodeList[newNodeList.size - 2]
        newNodeList[newNodeList.size - 1].parentNode = parentNode
        newNodeList[newNodeList.size - 1].fileNode = parentNode.fileNode
        newNodeList[newNodeList.size - 1].nextNode = nextNode

        // 将旧的节点删除，将新的节点插入
        parentNode.deleteChild(index)
        for (newNode in newNodeList) {
            parentNode.setChild(newNode, index)
            index++
        }
    }
}

