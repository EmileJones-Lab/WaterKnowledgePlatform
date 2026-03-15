package top.emilejones.hhu.textsplitter.adaptor

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.domain.pipeline.infrastructure.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.OcrGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.StructureExtractionGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.MinerUMarkdownFile
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.TextNodeDTO

import top.emilejones.hhu.common.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.preprocessor.SplitTextNodeTool
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

    override fun extract(inputStream: InputStream): TextNodeDTO {
        val result = MarkdownStructureParser(inputStream).get()
        SplitTextNodeTool(result, ragConfig.maxTableLength, ragConfig.maxSentenceLength).run()
        return result
    }

    override fun save(textNodeDTO: TextNodeDTO) {
        require(textNodeDTO.getChild(0).fileNode?.fileId != null) { "结构片段不属于任何文件，请查看代码逻辑" }
        neo4jRepository.insertTree(textNodeDTO)
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
