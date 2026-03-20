package top.emilejones.hhu.infrastructure.configuration.beans

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig

@Configuration
class Neo4jConfiguration {

    @Bean
    fun neo4jDriver(neo4jConfig: Neo4jConfig): Driver {
        return GraphDatabase.driver(
            "bolt://${neo4jConfig.host}:${neo4jConfig.port}",
            AuthTokens.basic(neo4jConfig.user, neo4jConfig.password)
        )
    }
}
