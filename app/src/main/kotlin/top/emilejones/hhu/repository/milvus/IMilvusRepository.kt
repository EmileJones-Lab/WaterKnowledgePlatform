package top.emilejones.hhu.repository.milvus

import top.emilejones.hhu.repository.milvus.po.EmbeddingDatum

interface IMilvusRepository: AutoCloseable {
    fun insert(datum: EmbeddingDatum): Boolean
    fun batchInsert(data: List<EmbeddingDatum>): Boolean
    fun searchByVector(
        queryVector: List<Float>,
        topK: Int = 10,
        filter: String? = null
    ): List<EmbeddingDatum>
}