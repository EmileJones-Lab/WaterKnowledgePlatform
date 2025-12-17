package top.emilejones.hhu.textsplitter.adaptor

import org.junit.jupiter.api.AfterEach
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest(classes = [TextSplitterTestMain::class])
class Neo4jNodeRepositoryAdaptorTest {

    @Autowired
    private lateinit var adaptor: Neo4jNodeRepositoryAdaptor

    @Autowired
    private lateinit var neo4jRepository: INeo4jRepository

    @BeforeEach
    fun setup() {
        neo4jRepository.clearAllData()
    }

    @AfterEach
    fun tearDown() {
        neo4jRepository.clearAllData()
    }


    @Test
    fun `find methods should map stored nodes to domain objects`() {
        val sample = insertSampleTree()

        val fileNode = adaptor.findFileNodeByFileNodeId(sample.fileNodeId)
        assertTrue(fileNode.isPresent)
        assertEquals(sample.fileId, fileNode.get().sourceDocumentId)
        assertFalse(fileNode.get().isEmbedded)

        val textNodes = adaptor.findTextNodeListByFileNodeId(sample.fileNodeId)
        assertEquals(2, textNodes.size)
        val orderedBySeq = textNodes.sortedBy { it.seq }
        assertEquals(listOf("intro", "details"), orderedBySeq.map { it.id })
        assertEquals(listOf(sample.fileNodeId, sample.fileNodeId), orderedBySeq.map { it.fileNodeId })
        assertEquals(listOf("first text node", "second text node"), orderedBySeq.map { it.text })
    }

    @Test
    fun `findTextNodeByTextNodeId should return single node with file link`() {
        val sample = insertSampleTree()

        val found = adaptor.findTextNodeByTextNodeId(sample.textNodeIds.first())
        assertTrue(found.isPresent)
        assertEquals(sample.textNodeIds.first(), found.get().id)
        assertEquals(sample.fileNodeId, found.get().fileNodeId)
        assertNull(found.get().vector)
        assertFalse(found.get().isEmbedded)
    }

    @Test
    fun `saveTextNode should update existing node contents`() {
        val sample = insertSampleTree()

        val updated = TextNode(
            id = sample.textNodeIds.first(),
            fileNodeId = sample.fileNodeId,
            text = "updated content",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            isEmbedded = true,
            vector = listOf(0.1f, 0.2f, 0.3f)
        )

        adaptor.saveTextNode(updated)

        val stored = neo4jRepository.searchNeo4jTextNodeByNodeId(sample.textNodeIds.first())
        assertNotNull(stored)
        assertEquals("updated content", stored.text)
        assertEquals(listOf(0.1f, 0.2f, 0.3f), stored.vector)
    }

    @Test
    fun `deleteAllNodeByFileNodeId should remove file and related text nodes`() {
        val sample = insertSampleTree()

        adaptor.deleteAllNodeByFileNodeId(sample.fileNodeId)

        assertNull(neo4jRepository.searchNeo4jFileNodeByNodeId(sample.fileNodeId))
        sample.textNodeIds.forEach { id ->
            assertNull(neo4jRepository.searchNeo4jTextNodeByNodeId(id))
        }
    }

    @Test
    fun `findTextNodeListByFileNodeId should throw when file not exists`() {
        assertFailsWith<IllegalArgumentException> {
            adaptor.findTextNodeListByFileNodeId("missing-file-node")
        }
    }

    private fun insertSampleTree(): SampleTree {
        val fileNode = FileNodeDTO(id = "file-node-1", fileId = "file-001")
        val root = TextNodeDTO(
            id = "root",
            text = "",
            seq = -1,
            level = 0,
            type = TextType.NULL
        ).apply { this.fileNode = fileNode }

        val first = TextNodeDTO(
            id = "intro",
            text = "first text node",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = root
            this.fileNode = fileNode
        }

        val second = TextNodeDTO(
            id = "details",
            text = "second text node",
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
        return SampleTree(
            fileNodeId = fileNode.id,
            fileId = fileNode.fileId!!,
            textNodeIds = listOf(first.id, second.id)
        )
    }

    private data class SampleTree(
        val fileNodeId: String,
        val fileId: String,
        val textNodeIds: List<String>
    )
}
