package top.emilejones.hhu.textsplitter.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.emilejones.hhu.infrastructure.configuration.env.pojo.RAGConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.model.pojo.RerankResult
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.service.IRecallService
import java.util.stream.Collectors

/**
 * @author EmileJones
 */
@Service
class RecallService(
    private val milvusRepository: IMultiCollectionMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val client: ModelClient,
    private val ragConfig: RAGConfig
) : IRecallService {
    override fun recallText(query: String, collectionName: String): List<String> {
        return recallNode(query, collectionName).stream()
            .map { it.text }
            .toList()
    }

    override fun recallNode(query: String, collectionName: String): List<Neo4jTextNode> {
        val maxResultNumber = ragConfig.recallNumber

        // 从向量数据库中召回数据
        val queryVector: List<Float> = client.embedding(query)
        val searchResults = milvusRepository.searchByVector(collectionName, queryVector, 100)

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

    private fun generateCypherByNeo4jTextNodeList(Neo4jTextNodeList: List<Neo4jTextNode>): String {
        val list = Neo4jTextNodeList.stream()
            .map(Neo4jTextNode::elementId)
            .map { "\"" + it + "\"" }
            .collect(Collectors.joining(", ", "[", "]"))
        return "MATCH (n: Neo4jTextNode) WHERE elementId(n) IN %s MATCH (f:Neo4jFileNode)-[:CONTAIN]->(n) RETURN n,f".format(
            list
        )
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RecallService::class.java)
    }
}
