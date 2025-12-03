package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

/**
 * 文档切割任务仓储接口，管理结构抽取任务的生命周期。
 *
 * 约定：通用约束与实现细节以各方法注释为准。
 * @author EmileJones
 */
interface StructureExtractionMissionRepository {
    /**
     * 保存单个任务；如果标识已存在则覆盖旧记录。
     *
     * 约定：具备 upsert 语义，重复写入需覆盖旧任务。
     */
    fun save(structureExtractionMission: StructureExtractionMission)

    /**
     * 批量保存任务；遇到重复标识执行覆盖（upsert）。
     *
     * 约定：具备 upsert 语义，需保证批量操作中的单条失败可定位。
     *
     * @param structureExtractionMissionList 待保存的任务集合
     */
    fun saveBatch(structureExtractionMissionList: List<StructureExtractionMission>)

    /**
     * 根据源文档查询任务列表。
     *
     * @param sourceDocumentId 源文档标识
     * @return 该文档相关的切割任务
     */
    fun findBySourceDocumentId(sourceDocumentId: String): List<StructureExtractionMission>

    /**
     * 批量查询任务列表。
     *
     * 约定：结果顺序需与入参保持一致；缺失项由实现决定返回空列表或空集合元素。
     *
     * @param sourceDocumentIdList 源文档标识列表
     * @return 与入参顺序一致的任务列表集合
     */
    fun findBySourceDocumentIdList(sourceDocumentIdList: List<String>): List<List<StructureExtractionMission>>

    /**
     * 删除任务。
     *
     * 约定：删除操作应幂等，重复删除不应抛出异常。
     *
     * @param structureExtractionMissionId 任务标识
     */
    fun delete(structureExtractionMissionId: String)
}
