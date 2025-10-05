package top.emilejones.hhu

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.emilejones.hhu.model.impl.ModelClientByHttp
import top.emilejones.hhu.repository.milvus.impl.MilvusRepositoryImpl
import top.emilejones.hhu.repository.neo4j.impl.Neo4jRepositoryImpl
import top.emilejones.hhu.service.impl.RagService
import top.emilejones.huu.env.AutoFindConfigFile
import top.emilejones.huu.env.pojo.ApplicationConfig
import java.io.File

/**
 * 命令行工具运行方式
 * @author EmileJones
 */
class Application : SuspendingCliktCommand() {
    private val config: ApplicationConfig = AutoFindConfigFile.find()
    private val dirPath: String by option().prompt("Directory path").help("The directory path of markdowns")
    private val logger: Logger = LoggerFactory.getLogger(Application::class.java)

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
        val modelClient = ModelClientByHttp(
            host = config.model.host,
            port = config.model.port,
            token = config.model.token,
            embeddingModel = config.model.embeddingModel,
            rerankModel = config.model.rerankModel
        )
        val ragService = RagService(
            milvusRepository = milvusRepository,
            neo4jRepository = neo4jRepository,
            modelClient = modelClient,
            maxSentenceLength = config.rag.maxSentenceLength,
            maxTableLength = config.rag.maxTableLength
        )
        File(dirPath).walk().forEach {
            if (it.isDirectory) {
                return@forEach
            }
            if (it.name.split('.').last().lowercase() != "md") {
                return@forEach
            }
            logger.info("Visit markdown file [{}]", it.name)
            ragService.saveMarkdownFileToAllDatabase(it.toPath())
        }
        ragService.close()
    }
}