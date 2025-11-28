package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

data class StartStructureExtractionMissionEvent(val structureExtractionMission: StructureExtractionMission): DomainEvent()