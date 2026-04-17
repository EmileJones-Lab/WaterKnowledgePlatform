package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asTextNode
import top.emilejones.hhu.textsplitter.service.IRecallService

/**
 * 向量库仓库的适配器实现，负责与 Milvus 进行交互。
 */
@Service
class TextNodeVectorRepositoryAdaptor(
    private val multiCollectionMilvusRepository: IMultiCollectionMilvusRepository,
    private val recallService: IRecallService,
    private val neo4jRepository: INeo4jRepository
) : TextNodeVectorRepository {

    override fun saveTextNodeToVectorDatabase(fileNodeIds: List<String>, collectionName: String): Result<Void> {
        return try {
            val allNodesToInsert = mutableListOf<EmbeddingDatum>()
            
            for (fileNodeId in fileNodeIds) {
                val fileNode = neo4jRepository.searchNeo4jFileNodeByNodeId(fileNodeId)
                if (fileNode == null) {
                    continue
                }
                val neo4jTextNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(fileNode.fileId)
                val textNodeList = neo4jTextNodeList.map { it.asTextNode(fileNode) }
                
                val nodesToInsert = textNodeList.filter { it.isEmbedded }
                    .map { convertTextNodeToEmbeddingDatum(it) }
                
                allNodesToInsert.addAll(nodesToInsert)
            }

            if (allNodesToInsert.isNotEmpty()) {
                multiCollectionMilvusRepository.batchInsert(collectionName, allNodesToInsert)
            }
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteTextNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void> {
        return try {
            multiCollectionMilvusRepository.batchDeleteByFileNodeIds(collectionName, fileNodeIds)
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createCollection(collectionName: String): Result<Void> {
        return try {
            multiCollectionMilvusRepository.createCollection(collectionName)
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun recallTextNode(query: String, collectionName: String, fileNodeIdList: List<String>?): List<TextNode> {
        val filter = if (fileNodeIdList.isNullOrEmpty()) {
            null
        } else {
            val ids = fileNodeIdList.joinToString("\", \"", "\"", "\"")
            "fileNodeId in [$ids]"
        }

        return recallService.recallNode(query, collectionName, filter)
            .map {
                val fileNode = neo4jRepository.searchNeo4jFileNodeByTextNode(it.id)
                it.asTextNode(fileNode)
            }.toList()
    }

    /**
     * 将TextNode转换为EmbeddingDatum。
     * 注意: TextNode必须被向量化过，否则会报错，请调用此方法时做好安全检查。
     */
    private fun convertTextNodeToEmbeddingDatum(textNode: TextNode): EmbeddingDatum {
        return EmbeddingDatum(
            vector = textNode.vector!!,
            neo4jNodeId = textNode.id,
            fileNodeId = textNode.fileNodeId
        )
    }
}
