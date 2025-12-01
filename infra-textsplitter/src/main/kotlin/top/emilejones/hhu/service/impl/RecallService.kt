package top.emilejones.hhu.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.dto.DenseRecallResult
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.model.pojo.RerankResult
import top.emilejones.hhu.repository.IMilvusRepository
import top.emilejones.hhu.repository.INeo4jRepository
import top.emilejones.hhu.service.IRecallService
import java.util.stream.Collectors

/**
 * @author EmileJones
 */
class RecallService(
    private val milvusRepository: IMilvusRepository,
    private val neo4jRepository: INeo4jRepository,
    private val client: ModelClient,
    private val recallNumber: Int
) : IRecallService {
    override fun recallText(query: String): List<String> {
        return recallNode(query).stream().map { datum: Pair<Neo4jFileNode?, Neo4jTextNode> -> datum.second.text }
            .toList()
    }

    override fun recallNode(query: String): List<Pair<Neo4jFileNode, Neo4jTextNode>> {
        val maxResultNumber = recallNumber

        // 从向量数据库中召回数据
        val queryVector: List<Float?> = client.embedding(query)
        val searchResults = milvusRepository.search(queryVector, 100)
        // 重排序结果，并取出得分最高的maxResultNumber个数据
        val rerankResult = client.rerank(query, searchResults!!.stream().map(DenseRecallResult::text).toList())
            .stream()
            .limit(maxResultNumber.toLong())
            .map { rr: RerankResult -> searchResults[rr.index] }.toList()
        // 将milvus数据转换为neo4j数据
        val rawData = rerankResult.mapNotNull { denseRecallResult: DenseRecallResult ->
            neo4jRepository.selectByElementId(denseRecallResult.elementId)
        }.toList()
        logger.debug(
            "查询问题[{}]召回节点的cypher语句为[{}]",
            query,
            generateCypherByNeo4jTextNodeList(rawData.stream().map { it.second }.toList())
        )
        logger.info("用户问题为：[{}]，召回的节点数量为[{}]个", query, rawData.size)
        return rawData
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
