package top.emilejones.hhu.textsplitter.parser

import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.FileNodeDTO
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.function.Supplier

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
        if (this.matches("!\\[([^\\]]*)\\]\\(([^)\\s]+)(?:\\s+\"([^\"]*)\")?\\)".toRegex())) return TextType.IMAGE
        return TextType.COMMON_TEXT
    }

/**
 * 将文件解析为树状结构
 * @author EmileJones
 */
class MarkdownStructureParser private constructor(
    private val lines: List<String>,
    private val fileName: String,
    private val fileId: String
) : Supplier<TextNodeDTO> {

    private var index = 0
    private var fileNode: FileNodeDTO
    private var preSeqNode: TextNodeDTO
    private var rootNode: TextNodeDTO
    private var isOver: Boolean

    private val logger = LoggerFactory.getLogger(MarkdownStructureParser::class.java)

    @Deprecated("这是本地文件测试使用的构造函数，已经弃用")
    constructor(file: File) : this(
        lines = file.readText(Charsets.UTF_8).lines(),
        fileName = file.name,
        fileId = ""
    )

    constructor(inputStream: InputStream, fileId: String) : this(
        lines = String(inputStream.readAllBytes()).lines(),
        fileName = "",
        fileId = fileId
    )

    constructor(inputStream: InputStream) : this(
        lines = String(inputStream.readAllBytes()).lines(),
        fileName = "",
        fileId = ""
    )

    init {
        isOver = false
        index = 0
        fileNode = FileNodeDTO(UUID.randomUUID().toString(), "")
        rootNode = TextNodeDTO(
            type = TextType.NULL,
            text = "",
            seq = -1,
            level = 0,
            id = UUID.randomUUID().toString()
        )
        rootNode.fileNode = fileNode
        preSeqNode = rootNode
    }

    /**
     * 将文件解析为树状结构，根节点为NULL类型节点，方便算法书写和后续处理，并无实际意义。
     * @return 树状结构的根节点
     */
    override fun get(): TextNodeDTO {
        if (isOver)
            return rootNode
        // 构建树状结构和序列结构
        while (index < lines.size)
            handleChild(rootNode)
        // 构建完成
        isOver = true
        return rootNode
    }

    private fun handleChild(parentNode: TextNodeDTO) {
        if (index >= lines.size)
            return

        if (lines[index].isEmpty()) {
            index++
            return
        }
        val nowIndex = index
        logger.debug("Parsing row [{}] of file [{}], line text: [{}]", nowIndex, fileName, lines[nowIndex])

        val nowNode: TextNodeDTO = TextNodeDTO(
            text = lines[nowIndex],
            seq = preSeqNode.seq + 1,
            level = lines[nowIndex].markdownLevel(),
            type = lines[nowIndex].textType,
            id = UUID.randomUUID().toString()
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

    private fun setFileRelationship(fileNode: FileNodeDTO, textNode: TextNodeDTO) {
        fileNode.addChild(textNode)
        textNode.fileNode = fileNode
    }

    private fun setParentRelationship(parent: TextNodeDTO, textNode: TextNodeDTO) {
        parent.addChild(textNode)
        textNode.parentNode = parent
    }

    private fun setPreSequenceRelationship(preSequenceNode: TextNodeDTO, textNode: TextNodeDTO) {
        preSequenceNode.nextNode = textNode
        textNode.preNode = preSequenceNode
    }
}
