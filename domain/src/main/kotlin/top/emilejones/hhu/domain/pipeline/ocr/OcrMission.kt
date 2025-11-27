package top.emilejones.hhu.domain.pipeline.ocr

import top.emilejones.hhu.domain.pipeline.MissionStatus
import top.emilejones.hhu.domain.pipeline.ProcessedDocument
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.OcrGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.SourceDocumentGateway
import java.time.Instant

class OcrMission(
    val id: String,
    val sourceDocumentId: String,
    status: MissionStatus,
    result: OcrMissionResult?,
    val createTime: Instant,
    var startTime: Instant?,
    var endTime: Instant?
) {

    private var status: MissionStatus = status

    var result: OcrMissionResult? = result
        private set

    init {
        sourceDocumentId.isNotBlank()
    }

    companion object {
        fun create(id: String, sourceDocumentId: String): OcrMission {
            return OcrMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                status = MissionStatus.PENDING,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    /** 启动 OCR 任务 */
    fun start(gateway: SourceDocumentGateway, ocrGateway: OcrGateway) {
        require(status == MissionStatus.PENDING) { "OCR can only start from PENDING state." }
        status = MissionStatus.RUNNING
        startTime = Instant.now()

        val sourceDocument = gateway.getSourceDocument(sourceDocumentId)
        if (sourceDocument == null) {
            failure("源文件不存在！")
            return
        }
        val result = runCatching { ocrGateway.startOcr(sourceDocument.inputStream) }
        if (result.isFailure) {
            val msg = result.exceptionOrNull()?.message ?: "未知的异常"
            failure(msg)
        }

        val processedDocument = result.getOrThrow()
        success(processedDocument)
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR

    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    /** 完成 OCR 任务，必须包含结果 */
    private fun success(processedDocument: ProcessedDocument) {
        require(status == MissionStatus.RUNNING) { "OCR can only complete from RUNNING state." }
        result = OcrMissionResult.Success(processedDocument)
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /** OCR 执行失败 */
    private fun failure(reason: String) {
        require(status == MissionStatus.RUNNING) { "OCR can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        result = OcrMissionResult.Failure(reason)
        endTime = Instant.now()
    }

}