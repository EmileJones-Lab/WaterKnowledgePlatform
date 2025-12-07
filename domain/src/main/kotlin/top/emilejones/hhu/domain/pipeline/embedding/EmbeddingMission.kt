package top.emilejones.hhu.domain.pipeline.embedding

import top.emilejones.hhu.domain.AggregateRoot
import top.emilejones.hhu.domain.pipeline.MissionStatus
import top.emilejones.hhu.domain.pipeline.event.EmbeddingMissionReadyEvent
import top.emilejones.hhu.domain.pipeline.event.EmbeddingMissionSuccessEvent
import java.time.Instant

/**
 * 向量化任务聚合根，负责驱动嵌入流程状态。
 * @author EmileJones
 */
class EmbeddingMission(
    override val id: String,
    val sourceDocumentId: String,
    var fileNodeId: String?,
    status: MissionStatus,
    result: EmbeddingMissionResult?,
    val createTime: Instant,
    var startTime: Instant?,
    var endTime: Instant?
) : AggregateRoot<String>(id) {

    var status: MissionStatus = status
        private set
    var result: EmbeddingMissionResult? = result
        private set


    companion object {
        /**
         * 创建初始化状态的向量化任务。
         */
        fun create(id: String, sourceDocumentId: String): EmbeddingMission {
            return EmbeddingMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                fileNodeId = null,
                status = MissionStatus.CREATED,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    /**
     * 将任务置为可调度状态并发布就绪事件。
     */
    fun preparedToExecution() {
        require(status == MissionStatus.CREATED) { "EmbeddingMission can only ready from CREATED" }
        status = MissionStatus.PENDING
        raiseEvent(EmbeddingMissionReadyEvent(this))
    }

    /**
     * 绑定文件节点后启动任务。
     */
    fun start(fileNodeId: String) {
        require(status == MissionStatus.PENDING) { "Embedding can only start from PENDING state." }
        this.fileNodeId = fileNodeId
        this.status = MissionStatus.RUNNING
        this.startTime = Instant.now()
    }

    /**
     * 任务成功，记录结果并发布完成事件。
     */
    fun success(fileNodeId: String) {
        require(status == MissionStatus.RUNNING) { "Embedding can only complete from RUNNING state." }
        result = EmbeddingMissionResult.Success(fileNodeId)
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
        raiseEvent(EmbeddingMissionSuccessEvent(this))
    }

    /**
     * 任务失败，记录失败原因。
     */
    fun failure(errorMessage: String) {
        require(status == MissionStatus.RUNNING || status == MissionStatus.PENDING) { "Embedding can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        endTime = Instant.now()
        this.result = EmbeddingMissionResult.Failure(errorMessage)
    }

    /**
     * 判断任务是否已进入终止态。
     */
    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR

    /**
     * 判断任务是否成功完成。
     */
    fun isSuccess(): Boolean = status == MissionStatus.SUCCESS

    /**
     * 获取成功结果，若状态异常会抛出异常。
     */
    fun getSuccessResult(): EmbeddingMissionResult.Success {
        require(status == MissionStatus.SUCCESS) {
            "任务没有成功，不可以获取成功的结果"
        }
        require(result is EmbeddingMissionResult.Success) {
            "任务成功但没有生成结果"
        }

        return result as EmbeddingMissionResult.Success
    }

    /**
     * 获取失败结果，若状态异常会抛出异常。
     */
    fun getFailureResult(): EmbeddingMissionResult.Failure {
        require(status == MissionStatus.ERROR) {
            "任务没有失败，不可以获取失败的结果"
        }
        require(result is EmbeddingMissionResult.Failure) {
            "任务失败但没有失败原因"
        }

        return result as EmbeddingMissionResult.Failure
    }
}
