package top.emilejones.hhu

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import top.emilejones.hhu.model.impl.ModelClientByClient
import top.emilejones.hhu.repository.milvus.impl.MilvusRepositoryImpl
import top.emilejones.hhu.repository.neo4j.impl.Neo4jRepositoryImpl
import top.emilejones.hhu.service.MilvusService
import top.emilejones.hhu.service.Neo4jService
import top.emilejones.huu.env.AutoFindConfigFile
import top.emilejones.huu.env.pojo.ApplicationConfig
import java.io.File

class Application : SuspendingCliktCommand() {
    private val config: ApplicationConfig = AutoFindConfigFile.find()
    private val dirPath: String by option().prompt("Directory path").help("The directory path of markdowns")
    override suspend fun run() {
        val milvusRepository = MilvusRepositoryImpl(
            port = config.milvus.port,
            host = config.milvus.host,
            databaseName = config.milvus.database,
            collectionName = config.milvus.collection
        )
        val neo4jRepository = Neo4jRepositoryImpl(
            username = config.neo4j.user,
            password = config.neo4j.password,
            host = config.neo4j.host,
            port = config.neo4j.port,
            databaseName = config.neo4j.database
        )
        val modelClient = ModelClientByClient(host = config.xinference.host, port =  config.xinference.port)
        val fileToNeo4jService = Neo4jService(neo4jRepository)
        val neo4jToMilvusService = MilvusService(milvusRepository, neo4jRepository, modelClient)
        File(dirPath).walk().forEach {
            if (it.isDirectory)
                return@forEach
            if (it.name.split('.').last().lowercase() != "md")
                return@forEach
            fileToNeo4jService.save(it)
            neo4jToMilvusService.saveByFilenameFromNeo4j(it.name)
        }

        fileToNeo4jService.close()
        neo4jToMilvusService.close()
    }
}