package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission

data class EmbeddingMissionReadyEvent(
    val embeddingMission: EmbeddingMission
) : DomainEvent()
