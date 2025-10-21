package top.emilejones.hhu.tools

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import top.emilejones.huu.env.AutoFindConfigFile

fun main() {
    val config = AutoFindConfigFile.find()
    val driver: Driver =
        GraphDatabase.driver(
            "bolt://${config.neo4j.host}:${config.neo4j.port}",
            AuthTokens.basic(config.neo4j.user, config.neo4j.password)
        )

    driver.session().use { session ->
        session.executeWriteWithoutResult {
            it.run("MATCH (n: TextNode | FileNode) DETACH DELETE n")
        }
    }
}