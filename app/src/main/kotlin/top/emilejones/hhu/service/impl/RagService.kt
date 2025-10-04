package top.emilejones.hhu.service.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.po.EmbeddingDatum
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.parser.MarkdownStructureParser
import top.emilejones.hhu.repository.milvus.IMilvusRepository
import top.emilejones.hhu.repository.neo4j.INeo4jRepository
import top.emilejones.hhu.service.IRagService
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

/**
 * @author EmileJones
 */
class RagService(
    private val milvusRepository: IMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient
) : IRagService, AutoCloseable {
    private val logger = LoggerFactory.getLogger(RagService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val semaphore = Semaphore(1)

    override suspend fun saveMarkdownFileToAllDatabase(filePath: Path) {
        saveFileInNeo4j(filePath.toFile())
        saveInMilvusFromNeo4jByFilename(filePath.name)
    }

    /**
     * 根据文件名称从Neo4j数据库中读取出整个文件的节点，将其存入向量数据库。
     * 如果这个文件曾经被成功存入过向量数据库，则不会再次被存入。
     * @param filename 需要存入的文件名
     */
    private suspend fun saveInMilvusFromNeo4jByFilename(filename: String) {
        logger.debug("Search all nodes about file [{}]", filename)
        val fileNode = neo4jRepository.searchNeo4jFileNodeByFileName(filename)
            ?: throw RuntimeException("The file [${filename}] is not exist")
        if (fileNode.isEmbedded) {
            logger.info("The file [{}] was embedded", filename)
            return
        }
        val textNodeList = neo4jRepository.searchNeo4jTextNodeByFilename(filename)
        logger.debug("Search nodes about file [{}] success! The nodes number is [{}]", filename, textNodeList.size)
        val embeddingData = textNodeList.map {
            scope.async {
                semaphore.withPermit {
                    delay(1000)
                    EmbeddingDatum(
                        vector = modelClient.embedding(it.text),
                        text = it.text,
                        neo4jElementId = it.elementId!!,
                        type = it.type
                    )
                }
            }
        }.awaitAll()
        logger.debug("Saving file [{}] in Milvus", filename)
        milvusRepository.batchInsert(embeddingData)
        neo4jRepository.updateNodeByElementId(fileNode.elementId!!, mapOf(Pair("isEmbedded", true)))
        logger.info("Success save all nodes about file [{}] in milvus", filename)
    }

    /**
     * 将Markdown文件存入图数据库中
     * @param file markdown文件
     */
    private fun saveFileInNeo4j(file: File) {
        val fileNode = neo4jRepository.searchNeo4jFileNodeByFileName(file.name)
        if (fileNode != null) {
            logger.info("The file [{}] is already exist in neo4j", file.name)
            return
        }

        logger.debug("Start read file [{}] as a tree structure", file.name)
        val parser = MarkdownStructureParser(file)
        val result = parser.run()
        logger.debug("Success read file [{}] as a tree structure", file.name)
        neo4jRepository.insertTree(result)
        logger.info("Success save tree structure of the file [{}] in neo4j", file.name)
    }

    override fun close() {
        milvusRepository.close()
        neo4jRepository.close()
    }
}