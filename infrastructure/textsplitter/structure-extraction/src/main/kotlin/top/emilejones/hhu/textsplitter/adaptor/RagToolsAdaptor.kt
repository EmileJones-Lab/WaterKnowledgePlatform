package top.emilejones.hhu.textsplitter.adaptor

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile

import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.preprocessor.SplitTextNodeTool
import top.emilejones.hhu.textsplitter.preprocessor.TextNodeLeafLevelProcessor
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.service.IDataProcessingService
import java.io.InputStream

@Service
class RagToolsAdaptor(
    private val dataProcessingService: IDataProcessingService,
    private val ragConfig: RAGConfig,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient,
    private val multiCollectionMilvusRepository: IMultiCollectionMilvusRepository
) : OcrGateway, StructureExtractionGateway, EmbeddingGateway {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun minerU(input: InputStream): MinerUMarkdownFile {
        return dataProcessingService.ocrFileToMarkdownFile(input).getOrThrow()
    }

    override fun extract(inputStream: InputStream, sourceDocumentId: String): String {
        val result = MarkdownStructureParser(inputStream).get()
        requireNotNull(result.fileNode).fileId = sourceDocumentId
        SplitTextNodeTool(result, ragConfig.maxTableLength, ragConfig.maxSentenceLength).run()
        TextNodeLeafLevelProcessor(result).run()
        neo4jRepository.insertTree(result)
        return requireNotNull(result.fileNode).id
    }

    override fun embed(textList: List<String>): List<List<Float>> = runBlocking(scope.coroutineContext) {
        val embeddingData = coroutineScope {
            textList.map {
                async {
                    modelClient.embedding(it)
                }
            }.awaitAll()
        }
        embeddingData
    }


    override fun saveTextNodeToVectorDatabase(textNodeList: List<TextNode>, collectionName: String) {
        textNodeList.forEach { require(it.isEmbedded) { "TextNode[${it.id}] 没有进行向量化" } }
        val embeddingData = textNodeList.map { convertTextNodeToEmbeddingDatum(it) }
        multiCollectionMilvusRepository.batchInsert(collectionName, embeddingData)
    }

    override fun deleteTextNodeFromVectorDatabases(textNodeIdList: List<String>, collectionName: String) {
        multiCollectionMilvusRepository.batchDelete(collectionName, textNodeIdList)
    }

    override fun createCollection(collectionName: String) {
        multiCollectionMilvusRepository.createCollection(collectionName)
    }

    /**
     * 将TextNode转换为EmbeddingDatum。
     * 注意: TextNode必须被向量化过，否则会报错，请调用此方法时做好安全检查。
     */
    private fun convertTextNodeToEmbeddingDatum(textNode: TextNode): EmbeddingDatum {
        return EmbeddingDatum(
            vector = textNode.vector!!,
            neo4jNodeId = textNode.id
        )
    }
}
