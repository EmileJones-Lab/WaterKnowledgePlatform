package top.emilejones.hhu.textsplitter.adaptor

import org.springframework.stereotype.Service
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

    override fun saveTextNodeToVectorDatabase(textNodeList: List<TextNode>, collectionName: String) {
        textNodeList.forEach { require(it.isEmbedded) { "TextNode[${it.id}] 没有进行向量化" } }
        val embeddingData = textNodeList.map { convertTextNodeToEmbeddingDatum(it) }
        multiCollectionMilvusRepository.batchInsert(collectionName, embeddingData)
    }

    override fun deleteTextNodeFromVectorDatabases(textNodeIdList: List<String>, collectionName: String) {
        multiCollectionMilvusRepository.batchDelete(collectionName, textNodeIdList)
    }

    override fun createTextNodeCollection(collectionName: String) {
        multiCollectionMilvusRepository.createCollection(collectionName)
    }

    override fun recallTextNode(query: String, collectionName: String): List<TextNode> {
        return recallService.recallNode(query, collectionName)
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
            neo4jNodeId = textNode.id
        )
    }
}
