package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

/**
 * 文档切割任务进入待调度状态的事件。
 * @author EmileJones
 */
data class StructureExtractionMissionReadyEvent(
    val structureExtractionMission: StructureExtractionMission
) : DomainEvent()
