package top.emilejones.hhu.domain.pipeline.ocr

import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

class OcrMission(
    val id: String,
    val sourceDocumentId: String,
    status: MissionStatus = MissionStatus.PENDING,
    result: OcrMissionResult? = null,
    val startTime: Instant = Instant.now(),
    var endTime: Instant? = null
) {

    private var status: MissionStatus = status
    private var result: OcrMissionResult? = result

    init {
        sourceDocumentId.isNotBlank()
    }

    /** 启动 OCR 任务 */
    fun start() {
        require(status == MissionStatus.PENDING) { "OCR can only start from PENDING state." }
        status = MissionStatus.RUNNING
    }

    /** 完成 OCR 任务，必须包含结果 */
    fun complete(processedDocumentId: String, processedDocumentPath: String) {
        require(status == MissionStatus.RUNNING) { "OCR can only complete from RUNNING state." }
        result = OcrMissionResult.Success(processedDocumentId, processedDocumentPath)
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /** OCR 执行失败 */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING) { "OCR can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        result = OcrMissionResult.Failure(reason)
        endTime = Instant.now()
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR
}