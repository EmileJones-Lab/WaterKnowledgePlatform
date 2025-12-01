package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

/**
 * OCR 任务仓储接口。
 * @author EmileJones
 */
interface OcrMissionRepository {
    /**
     * 保存单个任务；若已有同标识任务，将覆盖旧记录。
     */
    fun save(ocrMission: OcrMission)

    /**
     * 批量保存任务；遇到重复标识执行覆盖（upsert）。
     */
    fun saveBatch(ocrMissionList: List<OcrMission>)

    /**
     * 查询最近启动的 OCR 任务对应的源文件。
     */
    fun findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit: Int, offset: Int): List<String>

    /**
     * 根据源文档查询任务列表。
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<OcrMission>

    /**
     * 批量查询任务列表。
     */
    fun findBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<OcrMission>>
}
