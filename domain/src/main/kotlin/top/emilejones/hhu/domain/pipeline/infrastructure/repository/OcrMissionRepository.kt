package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

interface OcrMissionRepository {
    fun save(ocrMission: OcrMission)
    fun saveBatch(ocrMissionList: List<OcrMission>)
    fun findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit: Int, offset: Int): List<String>
    fun findBySourceDocumentId(sourceDocumentId: String): List<OcrMission>
    fun findBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<OcrMission>>
}