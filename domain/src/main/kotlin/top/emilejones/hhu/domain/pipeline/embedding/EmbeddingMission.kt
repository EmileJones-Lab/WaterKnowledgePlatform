package top.emilejones.hhu.domain.pipeline.embedding

import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

class EmbeddingMission @JvmOverloads constructor(
    val id: String,
    val sourceDocumentId: String,
    var neo4jFileNodeElementId: String? = null,
    status: MissionStatus = MissionStatus.PENDING,
    result: EmbeddingMissionResult? = null,
    val createTime: Instant = Instant.now(),
    var startTime: Instant? = null,
    var endTime: Instant? = null
) {
    var status: MissionStatus = status
        private set
    var result: EmbeddingMissionResult? = result
        private set

    fun start() {
        require(status == MissionStatus.PENDING) { "Embedding can only start from PENDING state." }
        require(!neo4jFileNodeElementId.isNullOrBlank()) { "neo4jFileNodeElementId is required" }
        status = MissionStatus.RUNNING
        startTime = Instant.now()
    }

    fun complete() {
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