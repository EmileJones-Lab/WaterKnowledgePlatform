package top.emilejones.hhu.textsplitter

import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.FileNodeDTO
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO
import kotlin.test.Test

/**
 * 简单遍历示例：从 NULL 根节点出发，打印整个树的结构。
 * 运行测试即可在输出中看到结构。
 */
class TextNodeTraversalSampleTest {

    @Test
    fun `print sample tree from null root`(): Unit {
        val fileNode = FileNodeDTO(id = "file-node-1", fileId = "file-001")
        val root = TextNodeDTO(
            id = "root",
            text = "",
            seq = -1,
            level = 0,
            type = TextType.NULL
        ).apply { this.fileNode = fileNode }

        val title = TextNodeDTO(
            id = "title-1",
            text = "Document Title",
            seq = 0,
            level = 1,
            type = TextType.TITLE
        ).apply {
            this.parentNode = root
            this.fileNode = fileNode
        }

        val paragraph1 = TextNodeDTO(
            id = "p-1",
            text = "First paragraph content.",
            seq = 0,
            level = 2,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = title
            this.fileNode = fileNode
        }

        val paragraph2 = TextNodeDTO(
            id = "p-2",
            text = "Second paragraph content.",
            seq = 1,
            level = 2,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = title
            this.fileNode = fileNode
            preNode = paragraph1
        }
        paragraph1.nextNode = paragraph2

        root.addChild(title)
        title.addChild(paragraph1)
        title.addChild(paragraph2)

        printTree(root)
    }
}


fun printTree(node: TextNodeDTO, indent: String = "") {
    println(
        "$indent- [${node.type}] id=${node.id}, seq=${node.seq}, level=${node.level}, text='${node.text}', file=${node.fileNode?.fileId}"
    )
    for (i in 0 until node.childNum()) {
        printTree(node.getChild(i), indent + "  ")
    }
}