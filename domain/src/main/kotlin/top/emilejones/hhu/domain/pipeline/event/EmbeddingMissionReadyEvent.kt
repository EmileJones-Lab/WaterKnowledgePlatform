package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

/**
 * 向量化任务进入待调度状态的事件。
 * @author EmileJones
 */
data class EmbeddingMissionReadyEvent(
    val embeddingMission: EmbeddingMission
) : DomainEvent()
