package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

data class StructureExtractionMissionReadyEvent(
    val structureExtractionMission: StructureExtractionMission
) : DomainEvent()
