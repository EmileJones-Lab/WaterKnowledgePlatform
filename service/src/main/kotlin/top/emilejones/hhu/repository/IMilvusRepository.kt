package top.emilejones.hhu.repository

import top.emilejones.hhu.domain.dto.DenseRecallResult
import top.emilejones.hhu.domain.po.EmbeddingDatum

/**
 * @author EmileJones
 */
interface IMilvusRepository : AutoCloseable {
    fun insert(datum: EmbeddingDatum): Boolean
    fun batchInsert(data: List<EmbeddingDatum>): Boolean
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

    fun clearAllData()
}