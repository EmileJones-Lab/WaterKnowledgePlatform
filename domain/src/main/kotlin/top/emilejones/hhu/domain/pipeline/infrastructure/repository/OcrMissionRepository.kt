package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.ocr.OcrMission

/**
 * OCR 任务仓储接口，负责任务持久化与查询。
 *
 * 约定：通用约束与实现细节以各方法注释为准。
 * @author EmileJones
 */
interface OcrMissionRepository {
    /**
     * 保存单个任务；若已有同标识任务，将覆盖旧记录。
     *
     * 约定：具备 upsert 语义，重复写入需覆盖旧任务。
     */
    fun save(ocrMission: OcrMission)

    /**
     * 批量保存任务；遇到重复标识执行覆盖（upsert）。
     *
     * 约定：具备 upsert 语义；应保证部分失败可定位，必要时支持局部回滚或幂等重试。
     *
     * @param ocrMissionList 待保存的 OCR 任务集合
     */
    fun saveBatch(ocrMissionList: List<OcrMission>)

    /**
     * 查询最近启动的 OCR 任务对应的源文件。
     *
     * 约定：需支持 limit/offset 分页，并按创建时间倒序返回。
     *
     * @param limit 限制返回数量
     * @param offset 偏移量，用于分页
     * @return 源文件标识列表，按创建时间倒序
     */
    fun findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit: Int, offset: Int): List<String>

    /**
     * 根据源文档查询任务列表。
     *
     * 约定：未命中时返回空列表；若需要排序，按任务创建时间倒序返回。
     *
     * @param sourceDocumentId 源文档标识
     * @return 该文档关联的 OCR 任务列表
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<OcrMission>

    /**
     * 批量查询任务列表。
     *
     * 约定：结果顺序需与入参保持一致；缺失项由实现决定返回空列表或占位元素。
     *
     * @param sourceDocumentIdList 源文档标识集合
     * @return 与入参顺序一致的任务列表集合
     */
    fun findBatchBySourceDocumentId(sourceDocumentIdList: List<String>): List<List<OcrMission>>

    /**
     * 删除任务。
     *
     * 约定：删除操作应幂等，重复删除不应抛出异常；未命中时可静默返回。
     *
     * @param ocrMissionId 任务标识
     */
    fun delete(ocrMissionId: String)
}
