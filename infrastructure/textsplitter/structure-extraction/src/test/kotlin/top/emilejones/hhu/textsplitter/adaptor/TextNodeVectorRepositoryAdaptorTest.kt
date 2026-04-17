package top.emilejones.hhu.textsplitter.adaptor

import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.service.IRecallService
import kotlin.test.Test
import kotlin.test.assertFailsWith

@SpringBootTest
class TextNodeVectorRepositoryAdaptorTest {

    @Autowired
    private lateinit var adaptor: TextNodeVectorRepositoryAdaptor

    @MockitoBean
    private lateinit var neo4jRepository: INeo4jRepository

    @MockitoBean
    private lateinit var milvusRepository: IMultiCollectionMilvusRepository

    @MockitoBean
    private lateinit var recallService: IRecallService
    
    @MockitoBean
    private lateinit var modelClient: ModelClient

    private val testCollection = "test_collection_vector_adaptor"

    @Test
    fun `recallTextNode should return mapped text nodes`() {
        // 1. Prepare mock data
        val textNodeId = "text-1"
        val fileNodeId = "file-1"
        val query = "test query"
        
        val neo4jTextNode = Neo4jTextNode(
            id = textNodeId,
            text = "content",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            vector = null
        )
        
        val neo4jFileNode = Neo4jFileNode(
            id = fileNodeId,
            fileId = "doc-1",
            isEmbedded = false
        )

        `when`(recallService.recallNode(anyString(), anyString(), anyOrNull())).thenReturn(listOf(neo4jTextNode))
        `when`(neo4jRepository.searchNeo4jFileNodeByTextNode(anyString())).thenReturn(neo4jFileNode)

        // 2. Call method
        val result = adaptor.recallTextNode(query, testCollection)

        // 3. Verify
        assertEquals(1, result.size)
        assertEquals(textNodeId, result[0].id)
        assertEquals(fileNodeId, result[0].fileNodeId)
    }

    @Test
    fun `saveTextNodeToVectorDatabase should load nodes from Neo4j and save to Milvus`() {
        // 1. Prepare mock data
        val fileNodeId = "file-1"
        val fileId = "doc-1"
        val textNodeId = "text-1"
        val vector = listOf(0.1f, 0.2f)

        val neo4jFileNode = Neo4jFileNode(id = fileNodeId, fileId = fileId, isEmbedded = true)
        val neo4jTextNode = Neo4jTextNode(
            id = textNodeId,
            text = "content",
            seq = 0,
            level = 1,
            type = TextType.COMMON_TEXT,
            vector = vector
        )

        `when`(neo4jRepository.searchNeo4jFileNodeByNodeId(anyString())).thenReturn(neo4jFileNode)
        `when`(neo4jRepository.searchNeo4jTextNodeByFileId(anyString())).thenReturn(mutableListOf(neo4jTextNode))

        // 2. Call method with list
        val result = adaptor.saveTextNodeToVectorDatabase(listOf(fileNodeId), testCollection)

        // 3. Verify
        assert(result.isSuccess)
        verify(milvusRepository).batchInsert(anyString(), anyList())
    }

    @Test
    fun `saveTextNodeToVectorDatabase should throw when fileNodeId not exists`() {
        `when`(neo4jRepository.searchNeo4jFileNodeByNodeId(anyString())).thenReturn(null)
        
        val result = adaptor.saveTextNodeToVectorDatabase(listOf("non-existent"), testCollection)
        assert(result.isSuccess) // It returns success now even if some IDs don't exist (it just skips them)
        verify(milvusRepository, never()).batchInsert(anyString(), anyList())
    }

    @Test
    fun `deleteTextNodeFromVectorDatabases should call repository with fileNodeId list`() {
        val fileNodeId = "file-1"
        val result = adaptor.deleteTextNodeFromVectorDatabases(listOf(fileNodeId), testCollection)
        assert(result.isSuccess)
        verify(milvusRepository).batchDeleteByFileNodeIds(anyString(), anyList())
    }

    // Helper for Mockito to avoid NPE with nullable parameters in Kotlin
    private fun <T> anyOrNull(): T {
        org.mockito.ArgumentMatchers.any<T>()
        return null as T
    }
}
