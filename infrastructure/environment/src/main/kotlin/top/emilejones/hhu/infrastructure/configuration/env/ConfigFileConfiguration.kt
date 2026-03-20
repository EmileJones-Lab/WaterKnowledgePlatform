package top.emilejones.hhu.infrastructure.configuration.env

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.infrastructure.configuration.env.ApplicationConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.OpenAiConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MilvusConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MinerUConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MinioConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MysqlConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig

@Configuration
@EnableConfigurationProperties(ApplicationConfig::class)
class ConfigFileConfiguration {

    @Bean
    fun milvusConfig(applicationConfig: ApplicationConfig): MilvusConfig = applicationConfig.milvus

    @Bean
    fun neo4jConfig(applicationConfig: ApplicationConfig): Neo4jConfig = applicationConfig.neo4j

    @Bean
    fun modelConfig(applicationConfig: ApplicationConfig): OpenAiConfig = applicationConfig.openai

    @Bean
    fun ragConfig(applicationConfig: ApplicationConfig): RAGConfig = applicationConfig.rag

    @Bean
    fun mysqlConfig(applicationConfig: ApplicationConfig): MysqlConfig = applicationConfig.mysql

    @Bean
    fun minerUConfig(applicationConfig: ApplicationConfig): MinerUConfig = applicationConfig.minerU

    @Bean
    fun minioConfig(applicationConfig: ApplicationConfig): MinioConfig = applicationConfig.minio
}