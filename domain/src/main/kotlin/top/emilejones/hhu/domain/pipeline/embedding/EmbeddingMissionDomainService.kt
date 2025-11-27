package top.emilejones.hhu.domain.pipeline.embedding

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult

class EmbeddingMissionDomainService(
    val embeddingGateway: EmbeddingGateway
) {
    fun startStructureEmbeddingMission(
        structureExtractionMission: StructureExtractionMission,
        embeddingMission: EmbeddingMission
    ) {
        if (!structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务")
            return
        }

        require(structureExtractionMission.result is StructureExtractionMissionResult.Success) {
            "代码异常，成功结束的文本切割任务没有生成结果"
        }

        val structureExtractionResult = structureExtractionMission.result as StructureExtractionMissionResult.Success
        embeddingMission.fileNodeId = structureExtractionResult.fileNodeId

        embeddingMission.start()

        val result = runCatching { embeddingGateway.embed(embeddingMission.fileNodeId!!) }

        if (result.isFailure) {
            val msg = result.exceptionOrNull()?.message ?: "未知的异常"
            embeddingMission.failure(msg)
            return
        }

        embeddingMission.success()
    }
}