package top.emilejones.hhu.textsplitter.adaptor

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import top.emilejones.hhu.common.Result
import top.emilejones.hhu.common.toCommonResult
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.preprocessing.structure.MarkdownStructureExtractor
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.ocr.MinerUClient
import top.emilejones.hhu.textsplitter.parser.MarkdownStructureParser
import top.emilejones.hhu.textsplitter.preprocessor.TextNodeLeafLevelProcessor
import top.emilejones.hhu.textsplitter.preprocessor.TextNodeSummaryProcessor
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.service.ISummarizationService
import java.io.InputStream
import java.util.*

@Service
class RagToolsAdaptor(
    private val minerUClient: MinerUClient,
    private val markdownStructureExtractor: MarkdownStructureExtractor,
    private val neo4jRepository: INeo4jRepository,
    private val modelClient: ModelClient,
    private val summarizationService: ISummarizationService
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
        
        // 1. 查询基础设施层的 POJO
        val neo4jFileNode = neo4jRepository.searchNeo4jFileNodeByNodeId(finalFileNodeId)
            ?: throw IllegalAccessException("结构提取任务存在，但是切割后的FileNode不存在")

        // 2. 找到所有的 Neo4jTextNode (使用 POJO 列表)
        val neo4jTextNodeList = neo4jRepository.searchNeo4jTextNodeByFileId(neo4jFileNode.fileId)

        // 3. 收集待向量化的文本：FileNode 的 fileAbstract 和 TextNode 的 summary
        val textsToEmbed = mutableListOf<String>()

        val fileAbstract = neo4jFileNode.fileAbstract 
            ?: throw IllegalStateException("FileNode $finalFileNodeId 没有执行摘要生成任务。")
        textsToEmbed.add(fileAbstract)

        neo4jTextNodeList.forEach { node ->
            val summary = node.summary ?: throw IllegalStateException("FileNode $finalFileNodeId 没有执行摘要生成任务。")
            textsToEmbed.add(summary)
        }

        // 4. 批量向量化
        val vectors = embed(textsToEmbed)

        // 5. 更新 POJO 并保存回 Neo4j
        var vectorIndex = 0
        
        // 更新 FileNode 向量
        neo4jRepository.updateNodeById(neo4jFileNode.id, mapOf(
            "vector" to vectors[vectorIndex++],
            "isEmbedded" to true
        ))

        // 批量更新 TextNode 向量
        neo4jTextNodeList.forEach { node ->
            neo4jRepository.updateNodeById(node.id, mapOf(
                "vector" to vectors[vectorIndex++],
                "isEmbedded" to true
            ))
        }

        finalFileNodeId
    }.toCommonResult()


    private fun updateTreeSummary(rootNode: TextNodeDTO) {
        // 更新 FileNode 的 fileAbstract
        rootNode.fileNode?.let { fileNodeDTO ->
            val id = fileNodeDTO.id
            neo4jRepository.updateNodeById(
                id,
                mapOf("fileAbstract" to fileNodeDTO.fileAbstract)
            )
        }

        // 递归更新所有 TextNode 的 summary
        deepUpdateSummary(rootNode)
    }

    private fun deepUpdateSummary(node: TextNodeDTO) {
        if (node.type != TextType.NULL) {
            val id = node.id
            if (node.summary != null) {
                neo4jRepository.updateNodeById(
                    id,
                    mapOf("summary" to node.summary)
                )
            }
        }
        for (i in 0 until node.childNum()) {
            deepUpdateSummary(node.getChild(i))
        }
    }
}
