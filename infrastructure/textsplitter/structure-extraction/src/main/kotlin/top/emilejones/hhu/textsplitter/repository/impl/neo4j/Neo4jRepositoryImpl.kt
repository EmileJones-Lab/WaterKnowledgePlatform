package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.common.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertRelationship

@Repository
class Neo4jRepositoryImpl(
    private val driver: Driver, private val neo4jConfig: Neo4jConfig,
    private val neo4jContextRepository: Neo4jContextRepository,
    private val neo4jFileNodeRepository: Neo4jFileNodeRepository,
    private val neo4jTextNodeRepository: Neo4jTextNodeRepository,
    private val nodeTreeRepository: NodeTreeRepository
) : INeo4jRepository {

    override fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode {
        return neo4jTextNodeRepository.insertNeo4jTextNode(node)
    }

    override fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode {
        return neo4jFileNodeRepository.insertNeo4jFileNode(node)
    }


    override fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertRelationship(relationship)
        }
    }

    override fun searchNeo4jFileNodeByNodeId(id: String): Neo4jFileNode? {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByNodeId(id)
    }

    override fun insertTree(rootNode: TextNodeDTO) {
        nodeTreeRepository.insertTree(rootNode)
    }

    override fun searchNeo4jFileNodeByFileId(fileId: String): Neo4jFileNode? {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByFileId(fileId)
    }

    override fun searchNeo4jTextNodeByFileId(fileId: String): MutableList<Neo4jTextNode> {
        return neo4jTextNodeRepository.searchTextNodeByFileId(fileId)
    }

    override fun searchNeo4jTextNodeByNodeId(id: String): Neo4jTextNode? {
        return neo4jTextNodeRepository.searchTextNodeByNodeId(id)
    }

    override fun searchNeo4jFileNodeByTextNode(id: String): Neo4jFileNode {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByTextNode(id)
    }

    override fun batchSearchNeo4jTextNodeByNodeId(idList: List<String>): List<Neo4jTextNode> {
        return neo4jTextNodeRepository.batchSearchTextNodeByNodeId(idList)
    }

    override fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any?>) {
        val setCypher = needUpdatedAttr.keys.map { "n.${it} = ${'$'}${it}" }
            .joinToString(separator = ", \n", postfix = "\n", prefix = "SET ")
        val cypher = """
            MATCH (n)
            WHERE elementId(n) = ${'$'}elementId
            $setCypher
        """.trimIndent()
        val params = mutableMapOf<String, Any?>(Pair("elementId", elementId)) + needUpdatedAttr
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.run(cypher, params)
        }
    }

    override fun deleteTextNodeById(id: String) {
        neo4jTextNodeRepository.softDeleteTextNodeById(id)
    }

    override fun hardDeleteTextNodeById(id: String) {
        neo4jTextNodeRepository.hardDeleteTextNodeById(id)
    }

    override fun deleteFileNodeById(id: String) {
        neo4jFileNodeRepository.softDeleteFileNodeById(id)
    }

    override fun hardDeleteFileNodeById(id: String) {
        neo4jFileNodeRepository.hardDeleteFileNodeById(id)
    }

    override fun nextNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        return neo4jContextRepository.nextNode(id)
    }

    override fun preNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        return neo4jContextRepository.preNode(id)
    }

    override fun parent(id: String): Neo4jTextNode? {
        return neo4jContextRepository.parent(id)
    }

    override fun siblings(id: String): List<Neo4jTextNode> {
        return neo4jContextRepository.siblings(id)
    }

    override fun children(id: String): List<Neo4jTextNode> {
        return neo4jContextRepository.children(id)
    }


    override fun clearAllData() {
        driver.session().use { session ->
            session.executeWriteWithoutResult {
                it.run("MATCH (n: TextNode | FileNode) DETACH DELETE n")
            }
        }
    }
}
