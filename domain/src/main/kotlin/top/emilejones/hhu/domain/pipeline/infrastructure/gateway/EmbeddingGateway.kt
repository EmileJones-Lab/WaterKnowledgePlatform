package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

interface EmbeddingGateway {
    fun embed(fileNodeId: String)
}