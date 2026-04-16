package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository

/**
 * 向量库仓库的适配器实现，负责与 Milvus 进行交互。
 */
@Service
class TextNodeVectorRepositoryAdaptor(
    private val multiCollectionMilvusRepository: IMultiCollectionMilvusRepository
) : TextNodeVectorRepository {

    override fun saveTextNodeToVectorDatabase(textNodeList: List<TextNode>, collectionName: String) {
        textNodeList.forEach { require(it.isEmbedded) { "TextNode[${it.id}] 没有进行向量化" } }
        val embeddingData = textNodeList.map { convertTextNodeToEmbeddingDatum(it) }
        multiCollectionMilvusRepository.batchInsert(collectionName, embeddingData)
    }

    override fun deleteTextNodeFromVectorDatabases(textNodeIdList: List<String>, collectionName: String) {
        multiCollectionMilvusRepository.batchDelete(collectionName, textNodeIdList)
    }

    override fun createCollection(collectionName: String) {
        multiCollectionMilvusRepository.createCollection(collectionName)
    }

    /**
     * 将TextNode转换为EmbeddingDatum。
     * 注意: TextNode必须被向量化过，否则会报错，请调用此方法时做好安全检查。
     */
    private fun convertTextNodeToEmbeddingDatum(textNode: TextNode): EmbeddingDatum {
        return EmbeddingDatum(
            vector = textNode.vector!!,
            neo4jNodeId = textNode.id
        )
    }
}
