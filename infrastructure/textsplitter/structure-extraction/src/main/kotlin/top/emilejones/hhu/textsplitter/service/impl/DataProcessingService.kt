package top.emilejones.hhu.textsplitter.service.impl

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor
import top.emilejones.hhu.textsplitter.ocr.MinerUClient
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.preprocessor.SplitTextNodeTool
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.service.IDataProcessingService
import java.io.InputStream

/**
 * @author EmileJones
 */
@Deprecated(message = "这是DDD引入之前的设计，现在已经不在维护")
@Service
class DataProcessingService(
    private val milvusRepository: IMultiCollectionMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient,
    private val ragConfig: RAGConfig,
    private val minerUClient: MinerUClient,
    private val markdownStructureExtractor: MarkdownStructureExtractor
) : IDataProcessingService {
    private val logger = LoggerFactory.getLogger(DataProcessingService::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun ocrFileToMarkdownFile(fileInputStream: InputStream): Result<MinerUMarkdownFile> {
        return kotlin.runCatching {
            val markdownFile = minerUClient.ocr(fileInputStream)
            markdownFile.copy(markdownContent = markdownStructureExtractor.extract(markdownFile.markdownContent))
        }
    }

    override fun extractMarkdownStructure(fileId: String, inputStream: InputStream): Result<String> {
        val fileNode = neo4jRepository.searchNeo4jFileNodeByFileId(fileId)
        if (fileNode != null) {
            logger.info("The file [{}] is already exist in neo4j", fileId)
            return Result.success(fileNode.fileId)
        }

        logger.debug("Reading file [{}] as a tree structure", fileId)
        val result = MarkdownStructureParser(inputStream, fileId).get()
        logger.debug("Preprocessing file [{}] structure", fileId)
        SplitTextNodeTool(result, ragConfig.maxTableLength, ragConfig.maxSentenceLength).run()

        neo4jRepository.insertTree(result)
        logger.info("Success save tree structure of the file [{}] in neo4j", fileId)
        val existsFileNode = neo4jRepository.searchNeo4jFileNodeByFileId(fileId)!!

        return Result.success(existsFileNode.elementId!!)
    }

    override fun embedTextNodes(fileId: String): Result<Unit> = runCatching {
        runBlocking(scope.coroutineContext) {
            val fileNode = neo4jRepository.searchNeo4jFileNodeByFileId(fileId)
                ?: throw RuntimeException("The file [${fileId}] is not exist")
            if (fileNode.isEmbedded) {
                logger.info("The file [{}] was embedded", fileId)
                return@runBlocking
            }
            val textNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(fileId)
            logger.debug("Embedding file [{}]", fileId)
            val embeddingData = coroutineScope {
                textNodeList.map { textNode ->
                    async {
                        textNode.copy(vector = modelClient.embedding(textNode.text))
                    }
                }.awaitAll()
            }
            embeddingData.forEach {
                logger.debug("Insert vector field in TextNode [{}] ", it.elementId)
                neo4jRepository.updateNodeByElementId(it.elementId!!, mapOf(Pair("vector", it.vector!!)))
            }
            logger.debug("Changing FileNode [{}] field isEmbedding from false to true in Neo4j", fileId);
            neo4jRepository.updateNodeByElementId(fileNode.elementId!!, mapOf(Pair("isEmbedded", true)))
            logger.info("Success save all nodes about file [{}] in milvus", fileId)
        }
    }

    override fun saveTextNodeToMilvus(fileId: String, collectionName: String) {
        val fileNode = neo4jRepository.searchNeo4jFileNodeByFileId(fileId)
            ?: throw RuntimeException("The file [${fileId}] is not exist")
        if (!fileNode.isEmbedded)
            throw IllegalAccessException("The file [${fileId}] was not embedding")

        val textNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(fileId)
        val embeddingData = textNodeList.map { convertTextNodeToEmbeddingDatum(it, fileNode.id) }
        milvusRepository.batchInsert(collectionName, embeddingData)
    }

    /**
     * 将TextNode转换为EmbeddingDatum。
     * 注意: TextNode必须被向量化过，否则会报错，请调用此方法时做好安全检查。
     */
    private fun convertTextNodeToEmbeddingDatum(textNode: Neo4jTextNode, fileNodeId: String): EmbeddingDatum {
        return EmbeddingDatum(
            vector = textNode.vector!!,
            neo4jNodeId = textNode.elementId!!,
            fileNodeId = fileNodeId
        )
    }
}
