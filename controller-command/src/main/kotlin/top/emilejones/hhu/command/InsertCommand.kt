package top.emilejones.hhu.command

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.emilejones.hhu.milvus.SingleCollectionMilvusRepository
import top.emilejones.hhu.model.impl.ModelClientByHttp
import top.emilejones.hhu.neo4j.Neo4jRepositoryImpl
import top.emilejones.hhu.repository.IMilvusRepository
import top.emilejones.hhu.repository.INeo4jRepository
import top.emilejones.hhu.service.impl.DataProcessingService
import top.emilejones.huu.env.AutoFindConfigFile
import top.emilejones.huu.env.pojo.ApplicationConfig
import java.io.File

/**
 * 解析文件并插入到数据库中的命令
 * @author EmileJones
 */
class InsertCommand : SuspendingCliktCommand(name = "insert"), AutoCloseable {
    override fun help(context: Context): String =
        "Import Markdown files from the specified file or folder into the database."

    private val config: ApplicationConfig = AutoFindConfigFile.find()
    private val filePath: String by argument().help("文件或者文件夹的路径")
    private val logger: Logger = LoggerFactory.getLogger(Application::class.java)

    private val milvusRepository: IMilvusRepository = SingleCollectionMilvusRepository(
        port = config.milvus.port,
        host = config.milvus.host,
        database = config.milvus.database,
        collection = config.milvus.collection,
        dimension = config.model.dimension
    )
    private val neo4jRepository: INeo4jRepository = Neo4jRepositoryImpl(
        username = config.neo4j.user,
        password = config.neo4j.password,
        host = config.neo4j.host,
        port = config.neo4j.port,
        databaseName = config.neo4j.database
    )
    private val modelClient = ModelClientByHttp(
        host = config.model.host,
        port = config.model.port,
        token = config.model.token,
        embeddingModel = config.model.embeddingModel,
        rerankModel = config.model.rerankModel
    )
    private val ragService = DataProcessingService(
        milvusRepository = milvusRepository,
        neo4jRepository = neo4jRepository,
        modelClient = modelClient,
        maxSentenceLength = config.rag.maxSentenceLength,
        maxTableLength = config.rag.maxTableLength
    )

    override suspend fun run() {
        val file = File(filePath)
        if (file.isDirectory)
            insertDirectory(file)
        else if (file.isFile)
            insertFile(file)
    }

    private suspend fun insertDirectory(file: File) {
        file.walk().forEach {
            if (it.isDirectory) {
                return@forEach
            }
            if (it.name.split('.').last().lowercase() != "md") {
                return@forEach
            }
            insertFile(it)
        }
    }

    private suspend fun insertFile(file: File) {
        logger.info("Visit markdown file [{}]", file.name)
        ragService.saveMarkdownFileToAllDatabase(file.toPath())
    }

    override fun close() {
        milvusRepository.close()
        neo4jRepository.close()
    }
}