package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

/**
 * 向量化任务仓储接口。
 * @author EmileJones
 */
interface EmbeddingMissionRepository {
    /**
     * 保存单个任务；已存在同标识任务时覆盖旧记录。
     */
    fun save(embeddingMission: EmbeddingMission)

    /**
     * 删除任务。
     */
    fun delete(embeddingMissionId: String)

    /**
     * 根据源文档查询任务列表。
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<EmbeddingMission>

    /**
     * 批量查询任务列表。
     */
    fun findBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<EmbeddingMission>>

    /**
     * 批量保存任务；遇到已存在的标识时执行覆盖（upsert）。
     */
    fun saveBatch(embeddingMissionList: List<EmbeddingMission>)

    /**
     * 将向量化结果保存到向量数据库中
     */
    fun saveToVectorDatabases(embeddingMissionId: String, collectionName: String)
}
