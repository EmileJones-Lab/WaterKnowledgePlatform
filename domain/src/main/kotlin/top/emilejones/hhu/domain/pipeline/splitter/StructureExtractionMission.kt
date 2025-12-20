package top.emilejones.hhu.domain.pipeline.splitter

import top.emilejones.hhu.domain.AggregateRoot
import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

/**
 * 文档结构切割任务聚合根，管理切分流程状态。
 * @author EmileJones
 */
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
        /**
         * 创建初始化状态的切割任务。
         */
        fun create(
            id: String,
            sourceDocumentId: String,
        ): StructureExtractionMission {
            return StructureExtractionMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                processedDocumentId = null,
                status = MissionStatus.PENDING,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    /**
     * 启动一个文本切割任务。
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
     * 文本切割任务完成。
     */
    fun success(fileNodeId: String) {
        require(status == MissionStatus.RUNNING) {
            "StructureExtractionMission can only complete from RUNNING"
        }

        result = StructureExtractionMissionResult.Success(
            fileNodeId = fileNodeId
        )
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    /**
     * 文本切割任务失败。
     */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING || status == MissionStatus.PENDING) {
            "StructureExtractionMission can only fail from RUNNING"
        }

        result = StructureExtractionMissionResult.Failure(reason)
        status = MissionStatus.ERROR
        endTime = Instant.now()
    }

    /**
     * 判断任务是否完成（成功或失败）。
     */
    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR

    /**
     * 判断任务是否成功。
     */
    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    /**
     * 获取成功结果，若状态不匹配则抛出异常。
     */
    fun getSuccessResult(): StructureExtractionMissionResult.Success {
        require(status == MissionStatus.SUCCESS) {
            "任务没有成功，不可以获取成功的结果"
        }
        require(result is StructureExtractionMissionResult.Success) {
            "任务成功但没有生成结果"
        }

        return result as StructureExtractionMissionResult.Success
    }

    /**
     * 获取失败结果，若状态不匹配则抛出异常。
     */
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
