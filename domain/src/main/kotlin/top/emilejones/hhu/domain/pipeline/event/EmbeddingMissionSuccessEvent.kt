package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

/**
 * 向量化任务成功完成的事件。
 * @author EmileJones
 */
data class EmbeddingMissionSuccessEvent(
    val embeddingMission: EmbeddingMission
) : DomainEvent()
