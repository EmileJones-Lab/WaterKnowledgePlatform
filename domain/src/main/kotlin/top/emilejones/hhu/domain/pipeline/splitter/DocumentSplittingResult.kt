package top.emilejones.hhu.domain.pipeline.splitter

sealed class DocumentSplittingResult {
    data class Success(
        val fileNodeId: String,
        val extractedTextNodeCount: Int
    ) : DocumentSplittingResult()

    data class Failure(
        val errorMessage: String
    ) : DocumentSplittingResult()
}