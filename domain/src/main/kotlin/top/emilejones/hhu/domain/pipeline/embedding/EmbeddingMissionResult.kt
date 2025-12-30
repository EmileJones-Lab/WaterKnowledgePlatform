package top.emilejones.hhu.domain.pipeline.embedding

/**
 * 向量化任务结果模型。
 * @author EmileJones
 */
sealed class EmbeddingMissionResult {
    /**
     * 成功结果，指向被向量化的FileNode节点。
     */
    data class Success(
        val fileNodeId: String
    ) : EmbeddingMissionResult()

    /**
     * 失败结果，记录错误信息。
     */
    data class Failure(
        val errorMessage: String
    ) : EmbeddingMissionResult()
}
