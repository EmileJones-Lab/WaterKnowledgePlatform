package top.emilejones.huu.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationConfig(
    val milvus: MilvusConfig,
    val neo4j: Neo4jConfig,
    val xinference: XinferenceConfig,
    val rag: RAGConfig
)
