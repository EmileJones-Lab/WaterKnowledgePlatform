package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.common.toCommonVoidResult
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

    override fun saveFileNodeToVectorDatabase(fileNodes: List<FileNode>, collectionName: String): Result<Void> = runCatching {
        // 校验：所有节点必须具备向量
        if (fileNodes.any { it.vector == null }) {
            throw IllegalStateException("存在未向量化的文件节点，无法同步到向量数据库。")
        }

        val embeddingData = fileNodes.map { node ->
            FileNodeEmbeddingDatum(
                vector = node.vector!!,
                fileNodeId = node.id
            )
        }

        if (embeddingData.isNotEmpty()) {
            fileNodeMilvusRepository.batchInsert(collectionName, embeddingData)
        }
    }.toCommonVoidResult()

    override fun createCollection(collectionName: String): Result<Void> = runCatching {
        fileNodeMilvusRepository.createCollection(collectionName)
    }.toCommonVoidResult()

    override fun deleteFileNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void> = runCatching {
        fileNodeMilvusRepository.batchDeleteByFileNodeIds(collectionName, fileNodeIds)
        Unit
    }.toCommonVoidResult()

    override fun recallFileNode(query: String, collectionName: String): List<FileNode> = runCatching {
        val queryVector = modelClient.embedding(query)

        val searchResults = fileNodeMilvusRepository.searchByVector(
            collectionName = collectionName,
            queryVector = queryVector,
            topK = 5,
            filter = null
        )

        searchResults.mapNotNull { datum ->
            neo4jRepository.searchNeo4jFileNodeByNodeId(datum.fileNodeId)?.asFileNode()
        }
    }.getOrDefault(emptyList())
}
