package top.emilejones.hhu

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import top.emilejones.hhu.model.impl.XinferenceHttpClient
import top.emilejones.hhu.repository.milvus.impl.MilvusRepositoryImpl
import top.emilejones.hhu.repository.neo4j.impl.Neo4jRepositoryImpl
import top.emilejones.hhu.service.MilvusService
import top.emilejones.hhu.service.Neo4jService
import top.emilejones.huu.env.MilvusEnvironment
import top.emilejones.huu.env.Neo4jEnvironment
import top.emilejones.huu.env.XinferenceEnvironment
import java.io.File

class Application : SuspendingCliktCommand() {
    private val dirPath: String by option().prompt("Directory path").help("The directory path of markdowns")
    override suspend fun run() {
        val milvusRepository = MilvusRepositoryImpl(
            port = MilvusEnvironment.PORT,
            host = MilvusEnvironment.HOST,
            databaseName = MilvusEnvironment.DATABASE_NAME,
            collectionName = MilvusEnvironment.COLLECTION_NAME
        )
        val neo4jRepository = Neo4jRepositoryImpl(
            username = Neo4jEnvironment.USER,
            password = Neo4jEnvironment.PASSWORD,
            host = Neo4jEnvironment.HOST,
            port = Neo4jEnvironment.PORT,
            databaseName = Neo4jEnvironment.DATABASE
        )
        val modelClient = XinferenceHttpClient(XinferenceEnvironment.HOST, XinferenceEnvironment.PORT)
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