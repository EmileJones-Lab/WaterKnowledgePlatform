package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

/**
 * OCR 任务进入待调度状态的事件。
 * @author EmileJones
 */
data class OcrMissionReadyEvent(
    val ocrMission: OcrMission
) : DomainEvent()
