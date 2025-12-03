package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.TextNode

/**
 * 向量化网关，调用底层嵌入实现。
 * @author EmileJones
 */
interface EmbeddingGateway {
    /**
     * 将文本进行向量化，按入参顺序返回向量化结果
     * @param textList 需要向量化的文本
     */
    fun embed(textList: List<String>): List<List<Float>>

    /**
     * 将向量化后的文本节点存入到向量数据库中
     * @param textNodeList 已经向量化后的TextNode节点数据
     * @param collectionName 希望添加到向量数据库中的哪一个collection
     */
    fun saveTextNodeToVectorDatabase(textNodeList: List<TextNode>, collectionName: String)
}
