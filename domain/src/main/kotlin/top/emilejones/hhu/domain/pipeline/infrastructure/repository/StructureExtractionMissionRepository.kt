package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission

interface StructureExtractionMissionRepository {
    fun save(structureExtractionMission: StructureExtractionMission)
    fun saveBatch(structureExtractionMissionList: List<StructureExtractionMission>)
    fun selectBySourceDocumentId(sourceDocumentId: String): List<StructureExtractionMission>
    fun selectBySourceDocumentIdList(sourceDocumentIdList: List<String>): List<List<StructureExtractionMission>>
    fun delete(structureExtractionMissionId: String)
}