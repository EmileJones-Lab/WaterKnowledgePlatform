package top.emilejones.hhu.textsplitter.repository

import top.emilejones.hhu.textsplitter.domain.po.milvus.TextNodeEmbeddingDatum

/**
 * 文本节点向量库仓储接口，负责处理多集合下的文本向量存储与检索。
 * 
 * @author EmileJones
 */
interface ITextNodeMilvusRepository {
    /**
     * 插入单条文本向量及其元数据。
     *
     * @param collectionName 目标 collection 名称。
     * @param datum 需要写入的向量及元数据。
     * @return 写入是否成功。
     */
    fun insert(collectionName: String, datum: TextNodeEmbeddingDatum): Boolean

    /**
     * 批量插入多条文本向量及其元数据。
     *
     * @param collectionName 目标 collection 名称。
     * @param data 待写入的向量集合。
     * @return 写入是否成功。
     */
    fun batchInsert(collectionName: String, data: List<TextNodeEmbeddingDatum>): Boolean

    /**
     * 根据 fileNodeId 批量软删除指定 collection 中的文本节点记录。
     *
     * @param collectionName 目标 collection 名称。
     * @param fileNodeIds 需要删除的 fileNodeId 列表。
     * @return 删除是否成功。
     */
    fun batchDeleteByFileNodeIds(collectionName: String, fileNodeIds: List<String>): Boolean

    /**
     * 使用向量检索并返回完整文本节点元数据。
     *
     * @param collectionName 目标 collection 名称。
     * @param queryVector 用于检索的查询向量。
     * @param topK 返回的相似结果数量。
     * @param filter 可选过滤表达式。
     * @return 检索到的 TopK 结果，按相似度从高到低排序。
     */
    fun searchByVector(
        collectionName: String,
        queryVector: List<Float>,
        topK: Int = 10,
        filter: String? = null
    ): List<TextNodeEmbeddingDatum>

    /**
     * 删除指定 collection。
     *
     * @param collectionName 需要删除的 collection 名称。
     */
    fun dropCollection(collectionName: String)

    /**
     * 创建一个新的文本节点 Milvus collection。
     *
     * @param collectionName 要创建的 collection 的名称。
     */
    fun createCollection(collectionName: String)
}
