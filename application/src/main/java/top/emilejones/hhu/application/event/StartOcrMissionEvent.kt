package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

data class StartOcrMissionEvent(val ocrMission: OcrMission): DomainEvent()

