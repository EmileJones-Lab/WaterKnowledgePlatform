package top.emilejones.hhu.textsplitter.config

import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.env.pojo.MilvusConfig
import top.emilejones.hhu.env.pojo.Neo4jConfig

@Configuration
class BeanConfiguration {

    @Bean
    fun neo4jDriver(neo4jConfig: Neo4jConfig): Driver {
        return GraphDatabase.driver(
            "bolt://${neo4jConfig.host}:${neo4jConfig.port}",
            AuthTokens.basic(neo4jConfig.user, neo4jConfig.password)
        )
    }

    @Bean
    fun milvusClient(milvusConfig: MilvusConfig): MilvusClientV2 {
        return MilvusClientV2(
            ConnectConfig.builder()
                .uri("http://%s:%d".format(milvusConfig.host, milvusConfig.port))
                .build()
        )
    }
}