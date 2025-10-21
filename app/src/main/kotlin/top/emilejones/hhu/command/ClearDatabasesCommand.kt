package top.emilejones.hhu.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.collection.request.DropCollectionReq
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import top.emilejones.huu.env.AutoFindConfigFile

/**
 * 清空数据库的命令
 * @author EmileJones
 */
class ClearDatabasesCommand : SuspendingCliktCommand(name = "clear") {
    override fun help(context: Context): String = "Clear all stored data related to the RAG application"

    private val config = AutoFindConfigFile.find()
    private val sure: String by option()
        .choice("y", "Y", "n", "N")
        .prompt("Are you sure? [y/n]")


    override suspend fun run() {
        if (sure.lowercase() == "y") {
            clearNeo4jDB()
            clearMilvusDB()
            echo("Done!")
        }
    }

    private fun clearMilvusDB() {
        val client = MilvusClientV2(
            ConnectConfig.builder()
                .uri("http://${config.milvus.host}:${config.milvus.port}")
                .build()
        )
        val dropQuickSetupParam = DropCollectionReq.builder()
            .collectionName(config.milvus.collection)
            .databaseName(config.milvus.database)
            .build()
        client.dropCollection(dropQuickSetupParam)

        client.close()
    }

    private fun clearNeo4jDB() {
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

        driver.close()
    }
}