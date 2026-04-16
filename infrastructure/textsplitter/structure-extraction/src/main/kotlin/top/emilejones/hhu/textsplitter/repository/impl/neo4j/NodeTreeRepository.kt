package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.QueryRunner
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationshipType
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.delegates.elementId
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.*
import java.util.*

@Repository
class NodeTreeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {

    private val logger = LoggerFactory.getLogger(NodeTreeRepository::class.java)

    fun insertTree(rootNode: TextNodeDTO) {
        if (rootNode.fileNode == null)
            throw IllegalArgumentException("The fileNode of rootNode must be not null!")

        rootNode.getChild(0).preNode = null
        for (i in 0..<rootNode.childNum()) {
            rootNode.getChild(i).parentNode = null
        }

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.beginTransaction().use { transaction ->
                logger.debug("Start insert tree transaction")
                val result = runCatching {
                    val neo4jFileNode = transaction.insertFileNode(rootNode.fileNode!!.toNeo4jFileNode())
                    for (i in 0..<rootNode.childNum()) {
                        deepVisitAndInsertNode(neo4jFileNode, rootNode.getChild(i), transaction)
                    }
                }
                result.fold(
                    onSuccess = {
                        transaction.commit()
                        logger.debug("Success insert [{}] Tree, transaction commited.", rootNode.fileNode!!.fileId)
                    },
                    onFailure = {
                        transaction.rollback()
                        logger.error("Taking an exception when insert ${rootNode.fileNode!!.fileId} Tree", it)
                        throw RuntimeException(it)
                    }
                )

            }
        }
    }

    fun findTreeByFileNodeId(fileNodeId: String): TextNodeDTO {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                // 1. Fetch FileNode
                val fileNodeResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id}) RETURN f",
                    Values.parameters("id", fileNodeId)
                )
                if (!fileNodeResult.hasNext()) throw NoSuchElementException("FileNode not found: ${'$'}fileNodeId")
                val neo4jFileNode = fileNodeResult.single()["f"].asNode().asNeo4jFileNode()
                val fileNodeDTO = neo4jFileNode.toFileNodeDTO()
                fileNodeDTO.elementId = neo4jFileNode.elementId

                // 2. Fetch all TextNodes for this FileNode
                val nodesResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(t:TextNode) RETURN t",
                    Values.parameters("id", fileNodeId)
                )
                val nodes = nodesResult.list().map { it["t"].asNode().asNeo4jTextNode() }
                val dtoMap = nodes.associate { it.id to it.toTextNodeDTO() }
                // Set elementId for each DTO
                nodes.forEach { dtoMap[it.id]?.elementId = it.elementId }

                // 3. Fetch CHILD relationships
                val childRelResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(p:TextNode)-[:CHILD]->(c:TextNode) RETURN p.id as parentId, c.id as childId",
                    Values.parameters("id", fileNodeId)
                )
                while (childRelResult.hasNext()) {
                    val record = childRelResult.next()
                    val parentId = record["parentId"].asString()
                    val childId = record["childId"].asString()
                    val parentDTO = dtoMap[parentId]
                    val childDTO = dtoMap[childId]
                    if (parentDTO != null && childDTO != null) {
                        parentDTO.addChild(childDTO)
                        childDTO.parentNode = parentDTO
                    }
                }

                // 4. Fetch NEXT_SEQUENCE relationships
                val nextRelResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(p:TextNode)-[:NEXT_SEQUENCE]->(n:TextNode) RETURN p.id as preId, n.id as nextId",
                    Values.parameters("id", fileNodeId)
                )
                while (nextRelResult.hasNext()) {
                    val record = nextRelResult.next()
                    val preId = record["preId"].asString()
                    val nextId = record["nextId"].asString()
                    val preDTO = dtoMap[preId]
                    val nextDTO = dtoMap[nextId]
                    if (preDTO != null && nextDTO != null) {
                        preDTO.nextNode = nextDTO
                        nextDTO.preNode = preDTO
                    }
                }

                // 5. Create NULL rootNode
                val rootNode = TextNodeDTO(
                    id = UUID.randomUUID().toString(),
                    text = "",
                    seq = -1,
                    level = 0,
                    type = TextType.NULL
                )
                rootNode.fileNode = fileNodeDTO

                // 6. Find top-level nodes (those without parent) and add to root
                val topLevelNodes = dtoMap.values.filter { it.parentNode == null }.sortedBy { it.seq }
                topLevelNodes.forEach {
                    rootNode.addChild(it)
                }

                // Set fileNode for all nodes in dtoMap
                dtoMap.values.forEach { it.fileNode = fileNodeDTO }

                rootNode
            }
        }
    }

    private fun deepVisitAndInsertNode(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        val nowNeo4jTextNode = queryRunner.insertTextNode(nowNode.toNeo4jTextNode())
        nowNode.elementId = nowNeo4jTextNode.elementId
        insertAllRelationShip(fileNode, nowNode, queryRunner)

        for (i in 0..<nowNode.childNum()) {
            deepVisitAndInsertNode(fileNode, nowNode.getChild(i), queryRunner)
        }
    }

    private fun insertAllRelationShip(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        val nowNodeElementId = nowNode.elementId!!

        if (nowNode.parentNode != null) {
            // 孩子关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNode.parentNode!!.elementId!!,
                    endNodeElementId = nowNodeElementId,
                    type = Neo4jRelationshipType.CHILD
                )
            )

            // 父亲关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNodeElementId,
                    endNodeElementId = nowNode.parentNode!!.elementId!!,
                    type = Neo4jRelationshipType.PARENT
                )
            )
        }


        if (nowNode.preNode != null) {
            // 前序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNodeElementId,
                    endNodeElementId = nowNode.preNode!!.elementId!!,
                    type = Neo4jRelationshipType.PRE_SEQUENCE
                )
            )

            // 后序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeElementId = nowNode.preNode!!.elementId!!,
                    endNodeElementId = nowNodeElementId,
                    type = Neo4jRelationshipType.NEXT_SEQUENCE
                )
            )
        }

        // 文件包含关系
        queryRunner.insertRelationship(
            Neo4jRelationship(
                startNodeElementId = fileNode.elementId!!,
                endNodeElementId = nowNodeElementId,
                type = Neo4jRelationshipType.CONTAIN
            )
        )
    }
}
