package top.emilejones.hhu.domain.pipeline.ocr

import top.emilejones.hhu.domain.AggregateRoot
import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

/**
 * OCR 任务聚合根，管理识别流程状态与结果。
 * @author EmileJones
 */
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
        /**
         * 创建初始化状态的 OCR 任务。
         */
        fun create(id: String, sourceDocumentId: String): OcrMission {
            return OcrMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                status = MissionStatus.CREATED,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    /**
     * 置为可调度状态并发布就绪事件。
     */
    fun preparedToExecution() {
        require(status == MissionStatus.CREATED) { "OcrMission can only ready from CREATED" }
        status = MissionStatus.PENDING
    }

    /**
     * 启动 OCR 处理。
     */
    fun start() {
        require(status == MissionStatus.PENDING) { "OCR can only start from PENDING state." }
        status = MissionStatus.RUNNING
        startTime = Instant.now()
    }

    /**
     * 判断任务是否已终止。
     */
    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR

    /**
     * 判断任务是否成功。
     */
    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    /**
     * 标记成功并写入结果。
     */
    fun success(markdownDocumentId: String) {
        require(status == MissionStatus.RUNNING) { "OCR can only complete from RUNNING state." }
        result = OcrMissionResult.Success(markdownDocumentId)
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /**
     * 标记失败并记录原因。
     */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING || status == MissionStatus.PENDING) { "OCR can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        result = OcrMissionResult.Failure(reason)
        endTime = Instant.now()
    }

    /**
     * 获取成功结果，状态不正确时抛出异常。
     */
    fun getSuccessResult(): OcrMissionResult.Success {
        require(status == MissionStatus.SUCCESS) {
            "任务没有成功，不可以获取成功的结果"
        }
        require(result is OcrMissionResult.Success) {
            "任务成功但没有生成结果"
        }

        return result as OcrMissionResult.Success
    }

    /**
     * 获取失败结果，状态不正确时抛出异常。
     */
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
