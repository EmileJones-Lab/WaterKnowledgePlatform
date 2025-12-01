package top.emilejones.hhu.domain.pipeline.splitter

/**
 * 文档结构切割任务的结果。
 * @author EmileJones
 */
sealed class StructureExtractionMissionResult {
    /**
     * 成功结果，包含生成的文件节点与切分数量。
     */
    data class Success(
        val fileNodeId: String,
        val chunkNumber: Int
    ) : StructureExtractionMissionResult()

    /**
     * 失败结果，记录错误信息。
     */
    data class Failure(
        val errorMessage: String
    ) : StructureExtractionMissionResult()
}
