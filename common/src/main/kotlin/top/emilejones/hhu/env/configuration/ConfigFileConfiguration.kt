package top.emilejones.hhu.env.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.env.AutoFindConfigFile
import top.emilejones.hhu.env.pojo.*

@Configuration
class ConfigFileConfiguration {

    @Bean
    fun applicationConfig(): ApplicationConfig = AutoFindConfigFile.find()

    @Bean
    fun milvusConfig(applicationConfig: ApplicationConfig): MilvusConfig = applicationConfig.milvus

    @Bean
    fun neo4jConfig(applicationConfig: ApplicationConfig): Neo4jConfig = applicationConfig.neo4j

    @Bean
    fun modelConfig(applicationConfig: ApplicationConfig): HttpModelClientConfig = applicationConfig.model

    @Bean
    fun ragConfig(applicationConfig: ApplicationConfig): RAGConfig = applicationConfig.rag

    @Bean
    fun mysqlConfig(applicationConfig: ApplicationConfig): MysqlConfig = applicationConfig.mysql

    @Bean
    fun minerUConfig(applicationConfig: ApplicationConfig): MinerUConfig = applicationConfig.minerU
    
    @Bean
    open fun minioConfig(applicationConfig: ApplicationConfig): MinioConfig = applicationConfig.minio
}
