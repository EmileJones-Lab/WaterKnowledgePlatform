package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.common.toCommonVoidResult
import top.emilejones.hhu.domain.pipeline.repository.FileNodeVectorRepository
import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.textsplitter.domain.po.milvus.FileNodeEmbeddingDatum
import top.emilejones.hhu.textsplitter.repository.IFileNodeMilvusRepository
import top.emilejones.hhu.textsplitter.service.IRecallService

/**
 * 文件节点向量库仓库的适配器实现，负责与 Milvus 进行交互。
 */
@Service
class FileNodeVectorRepositoryAdaptor(
    private val fileNodeMilvusRepository: IFileNodeMilvusRepository,
    private val recallService: IRecallService
) : FileNodeVectorRepository {

    override fun saveFileNodeToVectorDatabase(fileNodes: List<FileNode>, collectionName: String): Result<Void> =
        runCatching {
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

    override fun deleteFileNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void> =
        runCatching {
            fileNodeMilvusRepository.batchDeleteByFileNodeIds(collectionName, fileNodeIds)
            Unit
        }.toCommonVoidResult()

    override fun recallFileNode(query: String, collectionName: String): List<FileNode> {
        return recallService.recallFileNode(query, collectionName)
    }
}
