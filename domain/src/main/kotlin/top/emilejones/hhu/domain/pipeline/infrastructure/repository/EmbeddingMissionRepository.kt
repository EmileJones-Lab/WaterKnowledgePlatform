package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.framwork.ConsistentBatchProcessor
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

/**
 * 向量化任务仓储接口，管理任务的持久化与向量入库。
 *
 * 约定：通用约束与实现细节以各方法注释为准。
 * @author EmileJones
 */
interface EmbeddingMissionRepository : ConsistentBatchProcessor<String, EmbeddingMission> {
    /**
     * 根据源文档查询任务列表。
     *
     * 约定：返回该文档关联的全部向量化任务；未命中时返回空列表，返回顺序按任务创建时间倒序。
     *
     * @param sourceDocumentId 源文档标识
     * @return 该文档关联的全部向量化任务
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<EmbeddingMission>

    /**
     * 批量查询任务列表。
     *
     * 约定：结果顺序需与入参列表保持一致；缺失项应返回空列表占位，确保位置一一对应。
     *
     * @param sourceDocumentIdList 源文档标识列表
     * @return 与入参一一对应的任务列表集合
     */
    fun findBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<EmbeddingMission>>

}
