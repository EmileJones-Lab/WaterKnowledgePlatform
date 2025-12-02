package top.emilejones.huu.env

import net.mamoe.yamlkt.Yaml
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.huu.env.pojo.ApplicationConfig
import top.emilejones.huu.env.pojo.HttpModelClientConfig
import top.emilejones.huu.env.pojo.MilvusConfig
import top.emilejones.huu.env.pojo.MysqlConfig
import top.emilejones.huu.env.pojo.Neo4jConfig
import top.emilejones.huu.env.pojo.RAGConfig
import java.io.File
import java.io.IOException

@Configuration
object AutoFindConfigFile : ConfigFileReader {
    private var config: ApplicationConfig? = null

    @Bean
    override fun find(): ApplicationConfig {
        if (config != null)
            return config as ApplicationConfig

        val yamlFile = File("./config.yml")
        if (!yamlFile.exists())
            throw IOException("Can't find the config file, please create [config.yml]")
        val yamlContent = yamlFile.readText()
        val applicationConfig = Yaml.decodeFromString(ApplicationConfig.serializer(), yamlContent)
        config = applicationConfig
        return applicationConfig
    }

    @Bean
    fun milvusConfig(): MilvusConfig = find().milvus

    @Bean
    fun neo4jConfig(): Neo4jConfig = find().neo4j

    @Bean
    fun modelConfig(): HttpModelClientConfig = find().model

    @Bean
    fun ragConfig(): RAGConfig = find().rag

    @Bean
    fun mysqlConfig(): MysqlConfig = find().mysql
}
