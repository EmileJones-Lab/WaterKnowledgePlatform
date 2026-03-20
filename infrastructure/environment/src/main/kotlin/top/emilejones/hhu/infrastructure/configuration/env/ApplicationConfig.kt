package top.emilejones.hhu.infrastructure.configuration.env

import kotlinx.serialization.Serializable
import org.springframework.boot.context.properties.ConfigurationProperties
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MilvusConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MinerUConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MinioConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MysqlConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.OpenAiConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig

@ConfigurationProperties("app")
@Serializable
data class ApplicationConfig(
    val milvus: MilvusConfig,
    val neo4j: Neo4jConfig,
    val openai: OpenAiConfig,
    val rag: RAGConfig,
    val mysql: MysqlConfig,
    val minerU: MinerUConfig,
    val minio: MinioConfig
)