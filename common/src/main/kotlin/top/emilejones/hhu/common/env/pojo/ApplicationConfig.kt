package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
@Serializable
data class ApplicationConfig(
    val milvus: MilvusConfig,
    val neo4j: Neo4jConfig,
    val model: HttpModelClientConfig,
    val rag: RAGConfig,
    val mysql: MysqlConfig,
    val minerU: MinerUConfig,
    val minio: MinioConfig
)
