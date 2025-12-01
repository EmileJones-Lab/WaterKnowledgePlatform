package top.emilejones.hhu.domain.pipeline.ocr

sealed class OcrMissionResult {
    data class Success(
        val markdownDocumentId: String
    ) : OcrMissionResult()

    data class Failure(
        val errorMessage: String
    ) : OcrMissionResult()
}