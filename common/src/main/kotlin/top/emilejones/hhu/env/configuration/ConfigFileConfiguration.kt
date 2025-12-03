package top.emilejones.hhu.env.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.env.pojo.*
import top.emilejones.hhu.env.AutoFindConfigFile

@Configuration
open class ConfigFileConfiguration {

    @Bean
    open fun applicationConfig(): ApplicationConfig = AutoFindConfigFile.find()

    @Bean
    open fun milvusConfig(applicationConfig: ApplicationConfig): MilvusConfig = applicationConfig.milvus

    @Bean
    open fun neo4jConfig(applicationConfig: ApplicationConfig): Neo4jConfig = applicationConfig.neo4j

    @Bean
    open fun modelConfig(applicationConfig: ApplicationConfig): HttpModelClientConfig = applicationConfig.model

    @Bean
    open fun ragConfig(applicationConfig: ApplicationConfig): RAGConfig = applicationConfig.rag

    @Bean
    open fun mysqlConfig(applicationConfig: ApplicationConfig): MysqlConfig = applicationConfig.mysql

    @Bean
    open fun minerUConfig(applicationConfig: ApplicationConfig): MinerUConfig = applicationConfig.minerUConfig
}
