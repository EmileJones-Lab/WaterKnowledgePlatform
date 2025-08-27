package top.emilejones.hhu.milvus

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import top.emilejones.huu.env.Neo4jEnvironment

fun main() {
    val driver: Driver =
        GraphDatabase.driver(
            "bolt://${Neo4jEnvironment.HOST}:${Neo4jEnvironment.PORT}",
            AuthTokens.basic(Neo4jEnvironment.USER, Neo4jEnvironment.PASSWORD)
        )

    driver.session().use { session ->
        session.executeWriteWithoutResult{
            it.run("MATCH (n) DETACH DELETE n")
        }
    }
}