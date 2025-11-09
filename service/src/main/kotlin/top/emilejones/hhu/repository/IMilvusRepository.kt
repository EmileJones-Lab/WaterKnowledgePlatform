package top.emilejones.hhu.repository

import top.emilejones.hhu.domain.dto.DenseRecallResult
import top.emilejones.hhu.domain.po.EmbeddingDatum

/**
 * @author EmileJones
 */
interface IMilvusRepository : AutoCloseable {
    /**
     * 插入数据
     *
     * @param datum 数据
     * @return 是否成功
     */
    fun insert(datum: EmbeddingDatum): Boolean

    /**
     * 批量插入
     *
     * @param data 数据集
     * @return 是否成功
     */
    fun batchInsert(data: List<EmbeddingDatum>): Boolean

    /**
     * 根据向量搜索结构
     * @param queryVector 查询向量
     * @param topK 需要获取前多少条数据
     * @param filter 筛选条件
     *
     * @return TopK个数据
     */
    fun searchByVector(
        queryVector: List<Float>,
        topK: Int = 10,
        filter: String? = null
    ): List<EmbeddingDatum>

    /**
     * 根据向量去查找最相近的结果
     *
     * @param queryVector 查询向量
     * @param topK        需要查找的结果数量
     * @return 最相似结果，返回的对象中只有text属性和elementId属性
     */
    fun search(queryVector: List<Float?>?, topK: Int): List<DenseRecallResult>?

    /**
     * 清除所有数据
     */
    fun clearAllData()
}