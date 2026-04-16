package top.emilejones.hhu.textsplitter.adaptor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import top.emilejones.hhu.TextSplitterTestMain
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import kotlin.test.AfterTest
import kotlin.test.Test

@SpringBootTest(classes = [TextSplitterTestMain::class])
class TextNodeVectorRepositoryAdaptorTest {

    @Autowired
    private lateinit var adaptor: TextNodeVectorRepositoryAdaptor

    @Autowired
    private lateinit var neo4jRepository: INeo4jRepository

    @Autowired
    private lateinit var milvusRepository: IMultiCollectionMilvusRepository

    @Autowired
    private lateinit var modelClient: ModelClient

    private val createdFileNodeIds = mutableListOf<String>()
    private val createdTextNodeIds = mutableListOf<String>()
    private val testCollection = "test_collection_vector_adaptor"

    @AfterTest
    fun tearDown() {
        createdTextNodeIds.forEach { neo4jRepository.hardDeleteTextNodeById(it) }
        createdFileNodeIds.forEach { neo4jRepository.hardDeleteFileNodeById(it) }
        createdTextNodeIds.clear()
        createdFileNodeIds.clear()
        // Note: we might want to drop collection but milvusRepository might not have it easily exposed or it's shared
    }

    @Test
    fun `recallTextNode should return mapped text nodes`() {
        // 1. Prepare data in Neo4j
        val sample = insertSampleTree()
        val textNodeId = sample.textNodeIds.first()
        val text = "first text node"

        // 2. Prepare data in Milvus
        // Get embedding for the text to ensure it can be recalled
        val vector = modelClient.embedding(text)
        
        val datum = EmbeddingDatum(
            neo4jNodeId = textNodeId,
            vector = vector
        )
        milvusRepository.insert(testCollection, datum)
        
        // Wait for Milvus consistency
        Thread.sleep(1000)

        // 3. Call method
        val result = adaptor.recallTextNode(text, testCollection)

        // 4. Verify
        // Should find at least one node (the one we just inserted)
        assertTrue(result.isNotEmpty(), "Should recall at least one node")
        val recalledNode = result.first { it.id == textNodeId }
        assertEquals(textNodeId, recalledNode.id)
        assertEquals(sample.fileNodeId, recalledNode.fileNodeId)
        assertEquals(text, recalledNode.text)
    }

    private fun insertSampleTree(): SampleTree {
        val fileNode = FileNodeDTO(id = "file-node-vector-1", fileId = "file-vector-001")
        val root = TextNodeDTO(
            id = "root-vector",
            text = "",
            seq = -1,
            level = 0,
            type = TextType.NULL
        ).apply { this.fileNode = fileNode }

        val first = TextNodeDTO(
            id = "intro-vector",
            text = "first text node",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT
        ).apply {
            this.parentNode = root
            this.fileNode = fileNode
        }

        val second = TextNodeDTO(
            id = "details-vector",
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

        createdFileNodeIds.add(fileNode.id)
        createdTextNodeIds.add(first.id)
        createdTextNodeIds.add(second.id)

        return SampleTree(
            fileNodeId = fileNode.id,
            fileId = fileNode.fileId,
            textNodeIds = listOf(first.id, second.id)
        )
    }

    private data class SampleTree(
        val fileNodeId: String,
        val fileId: String,
        val textNodeIds: List<String>
    )
}
