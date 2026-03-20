package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import top.emilejones.hhu.TextSplitterTestMain
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.repository.impl.milvus.MultiCollectionSingleCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.Neo4jRepositoryImpl
import top.emilejones.hhu.textsplitter.service.impl.DataProcessingService
import java.io.ByteArrayInputStream
import kotlin.test.*

@SpringBootTest(classes = [TextSplitterTestMain::class])
class RagToolsAdaptorTest {
    @MockitoBean
    private lateinit var milvusRepository: MultiCollectionSingleCollectionMilvusRepository

    @MockitoBean
    private lateinit var neo4jRepository: Neo4jRepositoryImpl

    @MockitoBean
    private lateinit var dataProcessingService: DataProcessingService

    @Autowired
    private lateinit var ragConfig: RAGConfig

    @Autowired
    private lateinit var modelClient: ModelClient

    @Autowired
    private lateinit var adaptor: RagToolsAdaptor

    private val testCollection = "test_rag_tools_adaptor"

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
        val result = MarkdownStructureParser(ByteArrayInputStream(markdown.toByteArray())).get()

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
            assertEquals(index, node.seq)
            assertEquals(expectedText, node.text)
            node = node.nextNode
        }
        assertNull(node)
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
}
