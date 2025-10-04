package top.emilejones.hhu.parser

import top.emilejones.hhu.domain.dto.FileNode
import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.enums.TextType
import java.io.File

private fun String.markdownLevel(): Int {
    if (!this.startsWith("#"))
        return Int.MAX_VALUE
    return this.count { it == '#' }
}

private fun String.isText(): Boolean {
    return !this.startsWith("#")
}

private val String.textType: TextType
    get() {
        if (this.startsWith("<table>")) return TextType.TABLE
        if (this.startsWith("#")) return TextType.TITLE
        if (this.matches("![.*?](.*?)".toRegex())) return TextType.IMAGE
        return TextType.COMMON_TEXT
    }

/**
 * 将文件解析为树状结构
 * @author EmileJones
 * @param file 需要解析的文件
 */
class MarkdownStructureParser(file: File) {
    private val lines: List<String> = file.readText(Charsets.UTF_8).lines()
    private val fileName: String = file.name

    private var index = 0
    private var fileNode: FileNode
    private var preSeqNode: TextNode
    private var rootNode: TextNode
    private var isOver: Boolean

    init {
        isOver = false
        index = 0
        fileNode = FileNode(
            fileName = fileName
        )
        rootNode = TextNode(
            type = TextType.NULL,
            text = "",
            seq = -1,
            level = 0
        )
        rootNode.fileNode = fileNode
        preSeqNode = rootNode
    }

    /**
     * 将文件解析为树状结构，根节点为空节点，方便算法书写和后续处理，并无实际意义。
     * @return 树状结构的根节点
     */
    fun run(): TextNode {
        if (isOver)
            return rootNode
        // 构建树状结构和序列结构
        while (index < lines.size)
            handleChild(rootNode)
        // 构建完成
        isOver = true
        return rootNode
    }

    private fun handleChild(parentNode: TextNode) {
        if (index >= lines.size)
            return

        val nowIndex = index

        val nowNode: TextNode = TextNode(
            text = lines[nowIndex],
            seq = preSeqNode.seq + 1,
            level = lines[nowIndex].markdownLevel(),
            type = lines[nowIndex].textType
        )
        // 插入父子关系、序列关系
        setParentRelationship(parentNode, nowNode)
        setPreSequenceRelationship(preSeqNode, nowNode)
        // 插入和文件的关系
        setFileRelationship(fileNode, nowNode)
        // 此段文本处理完毕，准备处理下一个文本
        index++
        preSeqNode = nowNode
        // 如果是正文内容，则回溯
        if (lines[nowIndex].isText()) {
            return
        }
        // 遍历属于自己的其他孩子
        while (index < lines.size) {
            if (lines[index].markdownLevel() <= nowNode.level)
                return
            handleChild(nowNode)
        }
    }

    private fun setFileRelationship(fileNode: FileNode, textNode: TextNode) {
        fileNode.addChild(textNode)
        textNode.fileNode = fileNode
    }

    private fun setParentRelationship(parent: TextNode, textNode: TextNode) {
        parent.addChild(textNode)
        textNode.parentNode = parent
    }

    private fun setPreSequenceRelationship(preSequenceNode: TextNode, textNode: TextNode) {
        preSequenceNode.nextNode = textNode
        textNode.preNode = preSequenceNode
    }
}