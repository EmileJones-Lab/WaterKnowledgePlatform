package top.emilejones.hhu.domain.pipeline.splitter

import top.emilejones.hhu.domain.pipeline.MissionStatus
import java.time.Instant

class StructureExtractionMission @JvmOverloads constructor(
    val id: String,
    val sourceDocumentId: String,
    var processedDocumentId: String? = null,
    val type: StructureExtractionMissionType,
    status: MissionStatus,
    result: StructureExtractionResult? = null,
    val createTime: Instant = Instant.now(),
    val startTime: Instant? = null,
    var endTime: Instant? = null
) {

    var status: MissionStatus = status
        private set

    var result: StructureExtractionResult? = result
        private set

    init {
        sourceDocumentId.isNotBlank()
    }

    /**
     * 启动一个文本切割任务
     */
    fun start() {
        require(status == MissionStatus.PENDING) {
            "StructureExtractionMission can only start from PENDING"
        }
        require(!processedDocumentId.isNullOrBlank()) {
            "processedDocumentId can't be null or blank"
        }
        status = MissionStatus.RUNNING
    }

    /**
     * 文本切割任务完成
     */
    fun complete(fileNodeId: String, textNodeCount: Int, processedResourcePath: String? = null) {
        require(status == MissionStatus.RUNNING) {
            "StructureExtractionMission can only complete from RUNNING"
        }

        result = StructureExtractionResult.Success(
            fileNodeId = fileNodeId,
            extractedTextNodeCount = textNodeCount
        )
        status = MissionStatus.SUCCESS
    }

    /**
     * 文本切割任务失败
     */
    fun failure(reason: String) {
        require(status == MissionStatus.RUNNING) {
            "StructureExtractionMission can only fail from RUNNING"
        }

        result = StructureExtractionResult.Failure(reason)
        status = MissionStatus.ERROR
    }

    fun isCompleted(): Boolean = status == MissionStatus.SUCCESS || status == MissionStatus.ERROR
}