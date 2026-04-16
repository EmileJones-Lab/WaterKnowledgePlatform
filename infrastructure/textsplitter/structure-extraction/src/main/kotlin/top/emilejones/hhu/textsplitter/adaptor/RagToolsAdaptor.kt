package top.emilejones.hhu.textsplitter.adaptor

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.common.toCommonResult
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository

import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.textsplitter.ocr.MinerUClient
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.preprocessor.SplitTextNodeTool
import top.emilejones.hhu.textsplitter.preprocessor.TextNodeLeafLevelProcessor
import top.emilejones.hhu.textsplitter.preprocessor.TextNodeSummaryProcessor
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.delegates.elementId
import top.emilejones.hhu.textsplitter.service.ISummarizationService
import java.io.InputStream
import java.util.Objects

@Service
class RagToolsAdaptor(
    private val minerUClient: MinerUClient,
    private val markdownStructureExtractor: MarkdownStructureExtractor,
    private val ragConfig: RAGConfig,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient,
    private val multiCollectionMilvusRepository: IMultiCollectionMilvusRepository,
    private val summarizationService: ISummarizationService,
    private val nodeRepository: NodeRepository
) : OcrGateway, StructureExtractionGateway, EmbeddingGateway {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun minerU(input: InputStream): Result<MinerUMarkdownFile> = runCatching {
        val markdownFile = minerUClient.ocr(input)
        val correctLevelMarkdown = markdownStructureExtractor.extract(markdownFile.markdownContent)
        markdownFile.copy(markdownContent = correctLevelMarkdown)
    }.toCommonResult()

    override fun extract(inputStream: InputStream, sourceDocumentId: String): Result<String> = runCatching {
        val result = MarkdownStructureParser(inputStream).get()
        requireNotNull(result.fileNode).fileId = sourceDocumentId
        SplitTextNodeTool(result, ragConfig.maxTableLength, ragConfig.maxSentenceLength).run()
        TextNodeLeafLevelProcessor(result).run()
        neo4jRepository.insertTree(result)
        requireNotNull(result.fileNode).id
    }.toCommonResult()

    override fun summary(sourceDocumentId: String): Result<String> = runCatching {
        val neo4jFileNode = neo4jRepository.searchNeo4jFileNodeByFileId(sourceDocumentId)
            ?: throw NoSuchElementException("未找到 fileId 为 [$sourceDocumentId] 的 FileNode")

        val rootNode = neo4jRepository.findTreeByFileNodeId(neo4jFileNode.id)
        TextNodeSummaryProcessor(rootNode, summarizationService).run()

        // 更新 Neo4j 中的摘要信息
        updateTreeSummary(rootNode)

        neo4jFileNode.id
    }.toCommonResult()

    @Deprecated(
        "建议使用 embed(fileNodeId: String) 以支持完整的领域逻辑封装",
        replaceWith = ReplaceWith("embed(fileNodeId)")
    )
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

    override fun embed(fileNodeId: String): Result<String> = runCatching {
        val finalFileNodeId = Objects.requireNonNull(fileNodeId)
        val fileNodeOptional = nodeRepository.findFileNodeByFileNodeId(finalFileNodeId)
        if (fileNodeOptional.isEmpty) {
            throw IllegalAccessException("结构提取任务存在，但是切割后的FileNode不存在")
        }

        val fileNode = fileNodeOptional.get()
        // 找到所有的TextNode
        val textNodeList = nodeRepository.findTextNodeListByFileNodeId(finalFileNodeId)

        // 向量化所有text属性
        val vectors = embed(textNodeList.map { it.text })

        // 为所有的TextNode添加vector属性
        for (i in textNodeList.indices) {
            textNodeList[i].saveVector(vectors[i])
        }

        // 为FileNode标记状态
        fileNode.markAsEmbedded()

        // 保存到Neo4j (通过nodeRepository接口)
        textNodeList.forEach { nodeRepository.saveTextNode(it) }
        nodeRepository.saveFileNode(fileNode)

        finalFileNodeId
    }.toCommonResult()

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


    private fun updateTreeSummary(rootNode: TextNodeDTO) {
        // 更新 FileNode 的 fileAbstract
        rootNode.fileNode?.let { fileNodeDTO ->
            val elementId = fileNodeDTO.elementId
            if (elementId != null) {
                neo4jRepository.updateNodeByElementId(
                    elementId,
                    mapOf("fileAbstract" to fileNodeDTO.fileAbstract)
                )
            }
        }

        // 递归更新所有 TextNode 的 summary
        deepUpdateSummary(rootNode)
    }

    private fun deepUpdateSummary(node: TextNodeDTO) {
        if (node.type != TextType.NULL) {
            val elementId = node.elementId
            if (elementId != null && node.summary != null) {
                neo4jRepository.updateNodeByElementId(
                    elementId,
                    mapOf("summary" to node.summary)
                )
            }
        }
        for (i in 0 until node.childNum()) {
            deepUpdateSummary(node.getChild(i))
        }
    }
}
