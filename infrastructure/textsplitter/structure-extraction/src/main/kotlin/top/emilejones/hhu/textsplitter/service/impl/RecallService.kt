package top.emilejones.hhu.textsplitter.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.model.pojo.RerankResult
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode
import top.emilejones.hhu.textsplitter.repository.IFileNodeMilvusRepository
import top.emilejones.hhu.textsplitter.repository.ITextNodeMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asFileNode
import top.emilejones.hhu.textsplitter.service.IRecallService
import java.util.stream.Collectors

/**
 * @author EmileJones
 */
@Service
class RecallService(
    private val textNodeMilvusRepository: ITextNodeMilvusRepository,
    private val fileNodeMilvusRepository: IFileNodeMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val client: ModelClient,
    private val ragConfig: RAGConfig
) : IRecallService {

    override fun recallNode(query: String, collectionName: String, filter: String?): List<Neo4jTextNode> {
        val maxResultNumber = ragConfig.recallNumber

        // 从向量数据库中召回数据
        val queryVector: List<Float> = client.embedding(query)
        val searchResults = textNodeMilvusRepository.searchByVector(collectionName, queryVector, 100, filter)

        // 从Neo4j获取文本节点
        val neo4jNodes = neo4jRepository.batchSearchNeo4jTextNodeByNodeId(searchResults.map { it.neo4jNodeId })

        // 重排序结果，并取出得分最高的maxResultNumber个数据
        val rerankResult = client.rerank(query, neo4jNodes.map { it.text })
            .stream()
            .limit(maxResultNumber.toLong())
            .map { rr: RerankResult -> neo4jNodes[rr.index] }.toList()

        logger.debug(
            "查询问题[{}]召回节点的cypher语句为[{}]",
            query,
            generateCypherByNeo4jTextNodeList(rerankResult)
        )
        logger.info("用户问题为：[{}]，召回的节点数量为[{}]个", query, rerankResult.size)
        return rerankResult
    }

    override fun recallFileNode(query: String, collectionName: String): List<FileNode> = runCatching {
        val queryVector = client.embedding(query)

        // 1. 向量召回 30 个节点
        val searchResults = fileNodeMilvusRepository.searchByVector(
            collectionName = collectionName,
            queryVector = queryVector,
            topK = 30,
            filter = null
        )

        // 2. 从 Neo4j 获取节点详情
        val fileNodes = searchResults.mapNotNull { datum ->
            neo4jRepository.searchNeo4jFileNodeByNodeId(datum.fileNodeId)?.asFileNode()
        }

        if (fileNodes.isEmpty()) return emptyList()

        // 3. 准备重排序的文本列表
        val textList = fileNodes.map { it.fileAbstract ?: "" }

        // 4. 调用重排序模型进行重排序
        val rerankResults = client.rerank(query, textList)

        // 5. 根据重排序结果取 Top 5（ModelClient 已按分数降序排列）
        rerankResults
            .take(5)
            .map { fileNodes[it.index] }
    }.getOrDefault(emptyList())

    private fun generateCypherByNeo4jTextNodeList(Neo4jTextNodeList: List<Neo4jTextNode>): String {
        val list = Neo4jTextNodeList.stream()
            .map(Neo4jTextNode::id)
            .map { "\"" + it + "\"" }
            .collect(Collectors.joining(", ", "[", "]"))
        return "MATCH (n: TextNode) WHERE n.id IN %s MATCH (f:FileNode)-[:CONTAIN]->(n) RETURN n,f".format(
            list
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RecallService::class.java)
    }
}
