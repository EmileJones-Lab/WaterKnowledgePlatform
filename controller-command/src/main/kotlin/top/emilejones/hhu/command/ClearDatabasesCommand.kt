package top.emilejones.hhu.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import top.emilejones.hhu.repository.impl.neo4j.Neo4jRepositoryImpl
import top.emilejones.hhu.repository.INeo4jRepository
import top.emilejones.hhu.common.env.AutoFindConfigFile

/**
 * 清空数据库的命令
 * @author EmileJones
 */
class ClearDatabasesCommand() : SuspendingCliktCommand(name = "clear") {
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
        } else {
            echo("Cancel!")
        }
    }

    private fun clearMilvusDB() {
        val milvusRepository: ISingleCollectionMilvusRepository = SingleCollectionSingleCollectionMilvusRepository(
            port = config.milvus.port,
            host = config.milvus.host,
            database = config.milvus.database,
            collection = config.milvus.collection,
            dimension = config.model.dimension
        )
        milvusRepository.clearAllData()
        milvusRepository.close()
    }

    private fun clearNeo4jDB() {
        val neo4jRepository: INeo4jRepository = Neo4jRepositoryImpl(
            username = config.neo4j.user,
            password = config.neo4j.password,
            host = config.neo4j.host,
            port = config.neo4j.port,
            databaseName = config.neo4j.database
        )
        neo4jRepository.clearAllData()
        neo4jRepository.close()
    }
}