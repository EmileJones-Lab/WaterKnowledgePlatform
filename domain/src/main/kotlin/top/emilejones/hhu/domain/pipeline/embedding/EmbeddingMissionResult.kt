package top.emilejones.hhu.domain.pipeline.embedding

/**
 * 向量化任务结果模型。
 * @author EmileJones
 */
sealed class EmbeddingMissionResult {
    /**
     * 成功结果，包含简单标识。
     */
    data class Success(
        val success: String = "Success"
    ) : EmbeddingMissionResult()

    /**
     * 失败结果，记录错误信息。
     */
    data class Failure(
        val errorMessage: String
    ) : EmbeddingMissionResult()
}
