package top.emilejones.hhu.domain.pipeline.ocr

import top.emilejones.hhu.domain.pipeline.ProcessedDocument

sealed class OcrMissionResult {
    data class Success(
        val processedDocument: ProcessedDocument
    ) : OcrMissionResult()

    data class Failure(
        val errorMessage: String
    ) : OcrMissionResult()
}