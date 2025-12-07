package top.emilejones.hhu.textsplitter.adaptor

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.emilejones.hhu.TextSplitterTestMain
import top.emilejones.hhu.domain.pipeline.TextNode
import top.emilejones.hhu.domain.pipeline.TextType
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.FileNodeDTO
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(classes = [TextSplitterTestMain::class])
class Neo4jNodeRepositoryAdaptorTest {

    @Autowired
    private lateinit var adaptor: Neo4jNodeRepositoryAdaptor

    @Autowired
    private lateinit var neo4jRepository: INeo4jRepository

    @BeforeEach
    fun resetGraph() {
        neo4jRepository.clearAllData()
    }

    @Test
    fun `find methods should return persisted nodes`() {
        val (fileNodeId, textNodeIds) = insertSampleTree()

        val fileNode = adaptor.findFileNodeByFileNodeId(fileNodeId)
        assertTrue(fileNode.isPresent)
        assertEquals("file-001", fileNode.get().sourceDocumentId)

        val textNodes = adaptor.findTextNodeListByFileNodeId(fileNodeId)
        assertEquals(2, textNodes.size)
        val orderedById = textNodes.associateBy { it.id }
        assertEquals("first text", orderedById[textNodeIds.first()]?.text)
        assertEquals("second text", orderedById[textNodeIds.last()]?.text)

        val single = adaptor.findTextNodeByTextNodeId(textNodeIds.first())
        assertTrue(single.isPresent)
        assertEquals(textNodeIds.first(), single.get().id)
        assertEquals(fileNodeId, single.get().fileNodeId)
    }

    @Test
    fun `saveTextNode should update existing node`() {
        val (fileNodeId, textNodeIds) = insertSampleTree()

        val updated = TextNode(
            id = textNodeIds.first(),
            fileNodeId = fileNodeId,
            text = "updated content",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            vector = listOf(0.1f, 0.2f, 0.3f),
            isEmbedded = true
        )

        adaptor.saveTextNode(updated)

        val stored = neo4jRepository.searchNeo4jTextNodeByNodeId(textNodeIds.first())
        assertNotNull(stored)
        assertEquals("updated content", stored.text)
        assertEquals(listOf(0.1f, 0.2f, 0.3f), stored.vector)
    }

    private fun insertSampleTree(): Pair<String, List<String>> {
        val fileNode = FileNodeDTO(id = "file-node-1", fileId = "file-001")
        val root = TextNodeDTO(
            id = "root",
            text = "",
            seq = -1,
            level = 0,
            type = TextType.NULL
        ).apply { this.fileNode = fileNode }

        val first = TextNodeDTO(
            id = "text-1",
            text = "first text",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = root
            this.fileNode = fileNode
        }

        val second = TextNodeDTO(
            id = "text-2",
            text = "second text",
            seq = 1,
            level = 1,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = root
            this.fileNode = fileNode
            this.preNode = first
        }
        first.nextNode = second

        root.addChild(first)
        root.addChild(second)

        neo4jRepository.insertTree(root)
        return fileNode.id to listOf(first.id, second.id)
    }
}
