package top.emilejones.hhu.domain.pipeline.splitter

import top.emilejones.hhu.domain.AggregateRoot
import top.emilejones.hhu.domain.pipeline.MissionStatus
import top.emilejones.hhu.domain.pipeline.event.StructureExtractionMissionReadyEvent
import java.time.Instant

class StructureExtractionMission(
    override val id: String,
    val sourceDocumentId: String,
    processedDocumentId: String?,
    status: MissionStatus,
    result: StructureExtractionMissionResult?,
    val createTime: Instant,
    var startTime: Instant?,
    var endTime: Instant?
) : AggregateRoot<String>(id) {


    var processedDocumentId: String? = processedDocumentId
        private set
    var status: MissionStatus = status
        private set
    var result: StructureExtractionMissionResult? = result
        private set

    init {
        sourceDocumentId.isNotBlank()
    }

    companion object {
        fun create(
            id: String,
            sourceDocumentId: String,
        ): StructureExtractionMission {
            return StructureExtractionMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                processedDocumentId = null,
                status = MissionStatus.CREATED,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    /**
     * 可以被执行
     */
    fun preparedToExecution() {
        require(status == MissionStatus.CREATED) {
            "StructureExtractionMission can only ready from CREATED"
        }
        this.status = MissionStatus.PENDING
        raiseEvent(StructureExtractionMissionReadyEvent(this))
    }

    /**
     * 启动一个文本切割任务
     */
    fun start(processedDocumentId: String) {
        require(status == MissionStatus.PENDING) {
            "StructureExtractionMission can only start from PENDING"
        }
        this.processedDocumentId = processedDocumentId
        this.status = MissionStatus.RUNNING
        this.startTime = Instant.now()
    }

    /**
     * 文本切割任务完成
     */
    fun success(fileNodeId: String, textNodeCount: Int) {
        require(status == MissionStatus.RUNNING) {
            "StructureExtractionMission can only complete from RUNNING"
        }

        result = StructureExtractionMissionResult.Success(
            fileNodeId = fileNodeId,
            chunkNumber = textNodeCount
        )
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /**
     * 文本切割任务失败
     */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING || status == MissionStatus.PENDING) {
            "StructureExtractionMission can only fail from RUNNING"
        }

        result = StructureExtractionMissionResult.Failure(reason)
        status = MissionStatus.ERROR
        endTime = Instant.now()
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR
    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    fun getSuccessResult(): StructureExtractionMissionResult.Success {
        require(status == MissionStatus.SUCCESS) {
            "任务没有成功，不可以获取成功的结果"
        }
        require(result is StructureExtractionMissionResult.Success) {
            "任务成功但没有生成结果"
        }

        return result as StructureExtractionMissionResult.Success
    }

    fun getFailureResult(): StructureExtractionMissionResult.Failure {
        require(status == MissionStatus.ERROR) {
            "任务没有失败，不可以获取失败的结果"
        }
        require(result is StructureExtractionMissionResult.Failure) {
            "任务失败但没有失败原因"
        }

        return result as StructureExtractionMissionResult.Failure
    }
}