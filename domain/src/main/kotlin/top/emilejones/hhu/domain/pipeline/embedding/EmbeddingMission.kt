package top.emilejones.hhu.domain.pipeline.embedding

import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

class EmbeddingMission(
    val id: String,
    val sourceDocumentId: String,
    var fileNodeId: String?,
    status: MissionStatus,
    result: EmbeddingMissionResult?,
    val createTime: Instant,
    var startTime: Instant?,
    var endTime: Instant?
) {
    var status: MissionStatus = status
        private set
    var result: EmbeddingMissionResult? = result
        private set

    companion object {
        fun create(id: String, sourceDocumentId: String): EmbeddingMission {
            return EmbeddingMission(
                id = id,
                sourceDocumentId = sourceDocumentId,
                fileNodeId = null,
                status = MissionStatus.PENDING,
                result = null,
                createTime = Instant.now(),
                startTime = null,
                endTime = null
            )
        }
    }

    fun start() {
        require(status == MissionStatus.PENDING) { "Embedding can only start from PENDING state." }
        require(!fileNodeId.isNullOrBlank()) { "fileNodeId is required" }
        status = MissionStatus.RUNNING
        startTime = Instant.now()
    }

    fun success() {
        require(status == MissionStatus.RUNNING) { "Embedding can only complete from RUNNING state." }
        result = EmbeddingMissionResult.Success()
        status = MissionStatus.SUCCESS
        endTime = Instant.now()
    }

    fun failure(errorMessage: String) {
        require(status == MissionStatus.RUNNING) { "Embedding can only fail from RUNNING state." }
        status = MissionStatus.ERROR
        endTime = Instant.now()
        this.result = EmbeddingMissionResult.Failure(errorMessage)
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR
}