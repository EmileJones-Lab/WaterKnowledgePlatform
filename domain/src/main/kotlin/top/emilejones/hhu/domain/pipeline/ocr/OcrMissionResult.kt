package top.emilejones.hhu.domain.pipeline.ocr

/**
 * OCR 任务执行结果。
 * @author EmileJones
 */
sealed class OcrMissionResult {
    /**
     * 成功结果，包含生成的 Markdown 文档标识。
     */
    data class Success(
        val markdownDocumentId: String
    ) : OcrMissionResult()

    /**
     * 失败结果，记录错误信息。
     */
    data class Failure(
        val errorMessage: String
    ) : OcrMissionResult()
}
