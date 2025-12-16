package top.emilejones.hhu.textsplitter.adaptor

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.emilejones.hhu.TextSplitterTestMain
import top.emilejones.hhu.domain.pipeline.TextNode
import top.emilejones.hhu.domain.pipeline.TextType
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.FileNodeDTO
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import java.io.ByteArrayInputStream
import kotlin.test.*

@SpringBootTest(classes = [TextSplitterTestMain::class])
class RagToolsAdaptorTest {
    @Autowired
    private lateinit var adaptor: RagToolsAdaptor

    @Autowired
    private lateinit var milvusRepository: IMultiCollectionMilvusRepository

    @Autowired
    private lateinit var neo4jRepository: INeo4jRepository

    private val testCollection = "test"

    @BeforeEach
    fun setup() {
        milvusRepository.clearAllData(testCollection)
        neo4jRepository.clearAllData()
    }

    @Test
    fun `minerU should delegate to data processing service`() {
        val input = javaClass.classLoader.getResourceAsStream("pdf/test.pdf")
        val result = adaptor.minerU(input!!)
        assertEquals(result.images.size, 3)
    }

    @Test
    fun `extract should parse markdown and build sequential nodes`() {
        val markdown = """
            # Title
            This is a long sentence that should be split; and continued.
            ## SubTitle
            This is subtitle content
            ## SubTitle2
            This is subtitle2 content
        """.trimIndent()
        val result = adaptor.extract(ByteArrayInputStream(markdown.toByteArray()))

        data class ExpectedNode(
            val type: TextType,
            val seq: Int,
            val level: Int,
            val text: String,
            val children: List<ExpectedNode> = emptyList()
        )

        val expectedTree = ExpectedNode(
            type = TextType.NULL,
            seq = -1,
            level = 0,
            text = "",
            children = listOf(
                ExpectedNode(
                    type = TextType.TITLE,
                    seq = 0,
                    level = 1,
                    text = "# Title",
                    children = listOf(
                        ExpectedNode(
                            type = TextType.COMMON_TEXT,
                            seq = 1,
                            level = Int.MAX_VALUE,
                            text = "This is a long sentence that should be split; and continued."
                        ),
                        ExpectedNode(
                            type = TextType.TITLE,
                            seq = 2,
                            level = 2,
                            text = "## SubTitle",
                            children = listOf(
                                ExpectedNode(
                                    type = TextType.COMMON_TEXT,
                                    seq = 3,
                                    level = Int.MAX_VALUE,
                                    text = "This is subtitle content"
                                )
                            )
                        ),
                        ExpectedNode(
                            type = TextType.TITLE,
                            seq = 4,
                            level = 2,
                            text = "## SubTitle2",
                            children = listOf(
                                ExpectedNode(
                                    type = TextType.COMMON_TEXT,
                                    seq = 5,
                                    level = Int.MAX_VALUE,
                                    text = "This is subtitle2 content"
                                )
                            )
                        )
                    )
                )
            )
        )

        assertNotNull(result.fileNode)
        val rootFileNode = result.fileNode

        fun assertTree(actual: TextNodeDTO, expected: ExpectedNode) {
            assertEquals(expected.type, actual.type)
            assertEquals(expected.seq, actual.seq)
            assertEquals(expected.level, actual.level)
            assertEquals(expected.text, actual.text)
            assertEquals(rootFileNode, actual.fileNode)
            assertEquals(expected.children.size, actual.childNum())
            expected.children.forEachIndexed { index, child ->
                assertTree(actual.getChild(index), child)
            }
        }

        assertTree(result, expectedTree)

        val expectedSequenceTexts = listOf(
            "# Title",
            "This is a long sentence that should be split; and continued.",
            "## SubTitle",
            "This is subtitle content",
            "## SubTitle2",
            "This is subtitle2 content"
        )

        var node = result.nextNode
        expectedSequenceTexts.forEachIndexed { index, expectedText ->
            assertNotNull(node)
            assertEquals(index, node!!.seq)
            assertEquals(expectedText, node!!.text)
            node = node!!.nextNode
        }
        assertNull(node)
    }

    @Test
    fun `save should persist tree when file id exists`() {
        val root = TextNodeDTO(
            id = "root",
            text = "",
            seq = -1,
            level = 0,
            type = TextType.NULL
        )
        val child = TextNodeDTO(
            id = "child",
            text = "content",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT
        )
        val fileNode = FileNodeDTO(id = "fileNode", fileId = "file-123")
        child.fileNode = fileNode
        child.parentNode = root
        root.fileNode = fileNode
        root.addChild(child)
        adaptor.save(root)
    }

    @Test
    fun `embed should call model client for each text`() {
        val texts = listOf("a", "bb", "ccc")
        val embeddings = adaptor.embed(texts)
        embeddings.forEach { require(it.isNotEmpty()) }
    }

    @Test
    fun `saveTextNodeToVectorDatabase should throw when node not embedded`() {
        val notEmbedded = TextNode(
            id = "1",
            fileNodeId = "file-1",
            text = "text",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            isEmbedded = false,
            vector = null
        )

        assertFailsWith<IllegalArgumentException> {
            adaptor.saveTextNodeToVectorDatabase(listOf(notEmbedded), testCollection)
        }
    }

    @Test
    fun `saveTextNodeToVectorDatabase should forward embedded data to repository`() {
        val embedded1 = TextNode(
            id = "1",
            fileNodeId = "file-1",
            text = "text",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            isEmbedded = true,
            vector = List(1024) { it.toFloat() }
        )

        val embedded2 = TextNode(
            id = "2",
            fileNodeId = "file-1",
            text = "text",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            isEmbedded = true,
            vector = List(1024) { it.toFloat() }
        )

        adaptor.saveTextNodeToVectorDatabase(listOf(embedded1, embedded2), testCollection)
    }

    @Test
    fun `deleteTextNodeFromVectorDatabase should soft delete`() {
        adaptor.deleteTextNodeFromVectorDatabases(listOf("1", "2"), testCollection)
    }
}
