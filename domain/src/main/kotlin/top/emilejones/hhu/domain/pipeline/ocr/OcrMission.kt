package top.emilejones.hhu.domain.pipeline.ocr

import top.emilejones.hhu.domain.AggregateRoot
import top.emilejones.hhu.domain.pipeline.MissionStatus
import top.emilejones.hhu.domain.pipeline.ProcessedDocument
import java.time.Instant

class OcrMission(
    override val id: String,
    val sourceDocumentId: String,
    status: MissionStatus,
    result: OcrMissionResult?,
    val createTime: Instant,
    var startTime: Instant?,
    var endTime: Instant?
) : AggregateRoot<String>(id) {

    var status: MissionStatus = status
        private set
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
    fun start() {
        require(status == MissionStatus.PENDING) { "OCR can only start from PENDING state." }
        status = MissionStatus.RUNNING
        startTime = Instant.now()
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR

    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    /** 完成 OCR 任务，必须包含结果 */
    fun success(processedDocument: ProcessedDocument) {
        require(status == MissionStatus.RUNNING) { "OCR can only complete from RUNNING state." }
        result = OcrMissionResult.Success(processedDocument)
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /** OCR 执行失败 */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING || status == MissionStatus.PENDING) { "OCR can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        result = OcrMissionResult.Failure(reason)
        endTime = Instant.now()
    }

    fun getSuccessResult(): OcrMissionResult.Success {
        require(status == MissionStatus.SUCCESS) {
            "任务没有成功，不可以获取成功的结果"
        }
        require(result is OcrMissionResult.Success) {
            "任务成功但没有生成结果"
        }

        return result as OcrMissionResult.Success
    }

    fun getFailureResult(): OcrMissionResult.Failure {
        require(status == MissionStatus.ERROR) {
            "任务没有失败，不可以获取失败的结果"
        }
        require(result is OcrMissionResult.Failure) {
            "任务失败但没有失败原因"
        }

        return result as OcrMissionResult.Failure
    }
}