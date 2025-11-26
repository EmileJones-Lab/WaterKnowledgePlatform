package top.emilejones.hhu.domain.pipeline.splitter

sealed class StructureExtractionResult {
    data class Success(
        val fileNodeId: String,
        val extractedTextNodeCount: Int
    ) : StructureExtractionResult()

    data class Failure(
        val errorMessage: String
    ) : StructureExtractionResult()
}