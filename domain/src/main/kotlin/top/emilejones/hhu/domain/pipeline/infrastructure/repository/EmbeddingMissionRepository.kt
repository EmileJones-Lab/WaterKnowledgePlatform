package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

interface EmbeddingMissionRepository {
    fun save(embeddingMission: EmbeddingMission)
    fun delete(embeddingMissionId: String)
    fun selectBySourceDocumentId(sourceDocumentId: String): List<EmbeddingMission>
    fun selectBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<EmbeddingMission>>
    fun saveBatch(embeddingMissionList: List<EmbeddingMission>)
}