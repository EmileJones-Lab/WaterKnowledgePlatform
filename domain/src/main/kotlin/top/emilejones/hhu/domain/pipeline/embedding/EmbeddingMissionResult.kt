package top.emilejones.hhu.domain.pipeline.embedding

sealed class EmbeddingMissionResult {
    data class Success(
        val success: String = "Success"
    ) : EmbeddingMissionResult()

    data class Failure(
        val errorMessage: String
    ) : EmbeddingMissionResult()
}