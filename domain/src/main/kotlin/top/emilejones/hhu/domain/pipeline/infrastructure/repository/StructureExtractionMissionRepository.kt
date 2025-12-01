package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

/**
 * 文档切割任务仓储接口。
 * @author EmileJones
 */
interface StructureExtractionMissionRepository {
    /**
     * 保存单个任务；如果标识已存在则覆盖旧记录。
     */
    fun save(structureExtractionMission: StructureExtractionMission)

    /**
     * 批量保存任务；遇到重复标识执行覆盖（upsert）。
     */
    fun saveBatch(structureExtractionMissionList: List<StructureExtractionMission>)

    /**
     * 根据源文档查询任务列表。
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<StructureExtractionMission>

    /**
     * 批量查询任务列表。
     */
    fun findBySourceDocumentIdList(sourceDocumentIdList: List<String>): List<List<StructureExtractionMission>>

    /**
     * 删除任务。
     */
    fun delete(structureExtractionMissionId: String)
}
