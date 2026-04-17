package top.emilejones.hhu.textsplitter.repository

import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum

/**
 * @author EmileJones
 */
interface IMultiCollectionMilvusRepository {
    /**
     * 插入单条向量及其元数据。
     *
     * 该方法会在指定的 Milvus collection 中写入一条记录，包含向量、元素 ID、原始文本与文本类型。
     * 调用方需要确保 collection 已存在或由具体实现负责创建。
     *
     * @param collectionName 目标 collection 名称。
     * @param datum 需要写入的向量及元数据。
     * @return 写入是否成功。
     */
    fun insert(collectionName: String, datum: EmbeddingDatum): Boolean

    /**
     * 批量插入多条向量及其元数据。
     *
     * 会一次性将传入列表中的所有 EmbeddingDatum 写入指定 collection，以提升吞吐。
     * 失败时具体实现应抛出异常或返回 false 以便上层处理。
     *
     * @param collectionName 目标 collection 名称。
     * @param data 待写入的向量集合。
     * @return 写入是否成功。
     */
    fun batchInsert(collectionName: String, data: List<EmbeddingDatum>): Boolean

    /**
     * 根据 fileNodeId 批量删除指定 collection 中的记录。
     *
     * @param collectionName 目标 collection 名称。
     * @param fileNodeIds 需要删除的 fileNodeId 列表。
     * @return 删除是否成功。
     */
    fun batchDeleteByFileNodeIds(collectionName: String, fileNodeIds: List<String>): Boolean

    /**
     * 使用向量检索并返回完整 EmbeddingDatum。
     *
     * @param collectionName 目标 collection 名称。
     * @param queryVector 用于检索的查询向量（非空维度）。
     * @param topK 返回的相似结果数量。
     * @param filter 可选过滤表达式，用于限定待检索的子集。
     * @return 检索到的 TopK 结果，按相似度从高到低排序。
     */
    fun searchByVector(
        collectionName: String,
        queryVector: List<Float>,
        topK: Int = 10,
        filter: String? = null
    ): List<EmbeddingDatum>

    /**
     * 删除指定 collection 中的所有数据。
     *
     * 具体实现是通过删除 collection 实现，调用后 collection 会被删除。
     *
     * @param collectionName 需要清空的 collection 名称。
     */
    fun dropCollection(collectionName: String)

    /**
     * 创建一个新的 Milvus collection。
     *
     * 该方法用于在 Milvus 中创建一个具有指定名称的新 collection。
     * 如果 collection 已存在，具体实现可能会选择忽略、抛出异常或进行其他处理。
     *
     * @param collectionName 要创建的 collection 的名称。
     */
    fun createCollection(collectionName: String)
}
