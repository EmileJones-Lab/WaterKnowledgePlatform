package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.pipeline.repository.FileNodeVectorRepository
import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.domain.po.milvus.FileNodeEmbeddingDatum
import top.emilejones.hhu.textsplitter.repository.IFileNodeMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asFileNode

/**
 * 文件节点向量库仓库的适配器实现，负责与 Milvus 进行交互。
 */
@Service
class FileNodeVectorRepositoryAdaptor(
    private val fileNodeMilvusRepository: IFileNodeMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient
) : FileNodeVectorRepository {

    override fun saveFileNodeToVectorDatabase(fileNodes: List<FileNode>, collectionName: String): Result<Void> {
        return try {
            val embeddingData = fileNodes.filter { it.isEmbedded && it.vector != null }
                .map { node ->
                    FileNodeEmbeddingDatum(
                        vector = node.vector!!,
                        fileNodeId = node.id
                    )
                }
            
            if (embeddingData.isNotEmpty()) {
                fileNodeMilvusRepository.batchInsert(collectionName, embeddingData)
            }
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createCollection(collectionName: String): Result<Void> {
        return try {
            fileNodeMilvusRepository.createCollection(collectionName)
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteFileNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void> {
        return try {
            fileNodeMilvusRepository.batchDeleteByFileNodeIds(collectionName, fileNodeIds)
            Result.successVoid()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun recallFileNode(query: String, collectionName: String, fileNodeIdList: List<String>?): List<FileNode> {
        return try {
            val queryVector = modelClient.embedding(query)
            
            val filter = if (fileNodeIdList.isNullOrEmpty()) {
                null
            } else {
                val ids = fileNodeIdList.joinToString("\", \"", "\"", "\"")
                "fileNodeId in [$ids]"
            }

            val searchResults = fileNodeMilvusRepository.searchByVector(
                collectionName = collectionName,
                queryVector = queryVector,
                topK = 10,
                filter = filter
            )

            searchResults.mapNotNull { datum ->
                neo4jRepository.searchNeo4jFileNodeByNodeId(datum.fileNodeId)?.asFileNode()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
