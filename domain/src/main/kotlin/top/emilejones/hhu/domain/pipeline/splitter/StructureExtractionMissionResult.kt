package top.emilejones.hhu.domain.pipeline.splitter

sealed class StructureExtractionMissionResult {
    data class Success(
        val fileNodeId: String,
        val chunkNumber: Int
    ) : StructureExtractionMissionResult()

    data class Failure(
        val errorMessage: String
    ) : StructureExtractionMissionResult()
}