package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

interface OcrMissionRepository {
    fun save(ocrMission: OcrMission)
    fun saveBatch(ocrMissionList: List<OcrMission>)
    fun selectStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit: Int, offset: Int): List<String>
    fun selectBySourceDocumentId(sourceDocumentId: String): List<OcrMission>
    fun selectBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<OcrMission>>
}