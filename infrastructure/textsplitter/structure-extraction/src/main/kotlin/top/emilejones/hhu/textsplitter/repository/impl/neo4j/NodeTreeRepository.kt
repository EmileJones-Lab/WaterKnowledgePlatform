package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.QueryRunner
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jRelationshipType
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.*
import java.util.*

@Repository
class NodeTreeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {

    private val logger = LoggerFactory.getLogger(NodeTreeRepository::class.java)

    /**
     * 递归插入整个文档树结构。
     * # 执行逻辑
     * 开启写事务，首先持久化文件节点；随后从根节点开始递归遍历文档树，依次插入文本节点并建立父子 (CHILD/PARENT)、序列 (NEXT/PRE_SEQUENCE) 以及所属 (CONTAIN) 关系；最后提交事务或在失败时回滚。
     * 
     * @param rootNode 树的虚拟根节点，必须包含有效的 fileNode 且至少有一个子节点
     * @throws IllegalArgumentException 参数非法时抛出
     * @throws RuntimeException 事务失败时抛出
     */
    fun insertTree(rootNode: TextNodeDTO) {
        if (rootNode.fileNode == null)
            throw IllegalArgumentException("The fileNode of rootNode must be not null!")

        if (rootNode.childNum() == 0)
            throw IllegalArgumentException("The rootNode must have at least one child!")

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
                        deepVisitAndInsertNode(rootNode.getChild(i), transaction)
                    }
                    for (i in 0..<rootNode.childNum()) {
                        deepVisitAndInsertRelationship(neo4jFileNode, rootNode.getChild(i), transaction)
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

    /**
     * 从数据库加载并重组指定文件的树状结构。
     *
     * # 执行逻辑
     * 在只读事务中分步执行：
     * 1. 获取文件节点；
     * 2. 获取该文件关联的所有文本节点；
     * 3. 批量查询并重建 CHILD/PARENT 关系；
     * 4. 批量查询并重建序列关系；
     * 5. 构建虚拟根节点并将顶层节点挂载，最终返回完整的内存树模型。
     * 
     * @param fileNodeId 文件节点唯一标识
     * @return 包含完整层级结构的虚拟根节点
     * @throws NoSuchElementException 若找不到指定的文件节点
     */
    fun findTreeByFileNodeId(fileNodeId: String): TextNodeDTO {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                // 1. Fetch FileNode
                val fileNodeResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id}) WHERE coalesce(f.isDelete, false) = false RETURN f",
                    Values.parameters("id", fileNodeId)
                )
                if (!fileNodeResult.hasNext()) throw NoSuchElementException("FileNode not found: ${'$'}fileNodeId")
                val neo4jFileNode = fileNodeResult.single()["f"].asNode().asNeo4jFileNode()
                val fileNodeDTO = neo4jFileNode.toFileNodeDTO()

                // 2. Fetch all TextNodes for this FileNode
                val nodesResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(t:TextNode) WHERE coalesce(f.isDelete, false) = false AND coalesce(t.isDelete, false) = false RETURN t",
                    Values.parameters("id", fileNodeId)
                )
                val nodes = nodesResult.list().map { it["t"].asNode().asNeo4jTextNode() }
                val dtoMap = nodes.associate { it.id to it.toTextNodeDTO() }

                // 3. Fetch CHILD relationships
                val childRelResult = tx.run(
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(p:TextNode)-[:CHILD]->(c:TextNode) WHERE coalesce(f.isDelete, false) = false AND coalesce(p.isDelete, false) = false AND coalesce(c.isDelete, false) = false RETURN p.id as parentId, c.id as childId",
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
                    "MATCH (f:FileNode {id: ${'$'}id})-[:CONTAIN]->(p:TextNode)-[:NEXT_SEQUENCE]->(n:TextNode) WHERE coalesce(f.isDelete, false) = false AND coalesce(p.isDelete, false) = false AND coalesce(n.isDelete, false) = false RETURN p.id as preId, n.id as nextId",
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

    /**
     * 深度优先遍历树节点并插入节点。
     *
     * @param nowNode 当前正在处理的节点
     * @param queryRunner 用于执行 Cypher 的驱动对象
     */
    private fun deepVisitAndInsertNode(
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        queryRunner.insertTextNode(nowNode.toNeo4jTextNode())

        for (i in 0..<nowNode.childNum()) {
            deepVisitAndInsertNode(nowNode.getChild(i), queryRunner)
        }
    }

    /**
     * 深度优先遍历树节点并插入关系。
     *
     * @param fileNode 关联的文件节点
     * @param nowNode 当前正在处理的节点
     * @param queryRunner 用于执行 Cypher 的驱动对象
     */
    private fun deepVisitAndInsertRelationship(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        insertAllRelationShip(fileNode, nowNode, queryRunner)

        for (i in 0..<nowNode.childNum()) {
            deepVisitAndInsertRelationship(fileNode, nowNode.getChild(i), queryRunner)
        }
    }

    /**
     * 为节点插入所有的关联关系。
     * 包括：CHILD（父到子）、PARENT（子到父）、PRE_SEQUENCE（后到前）、NEXT_SEQUENCE（前到后）以及 CONTAIN（文件到文本）。
     * 
     * @param fileNode 所属文件节点
     * @param nowNode 当前文本节点
     * @param queryRunner 用于执行 Cypher 的驱动对象
     */
    private fun insertAllRelationShip(
        fileNode: Neo4jFileNode,
        nowNode: TextNodeDTO,
        queryRunner: QueryRunner
    ) {
        val nowNodeId = nowNode.id

        if (nowNode.parentNode != null) {
            // 孩子关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeId = nowNode.parentNode!!.id,
                    endNodeId = nowNodeId,
                    type = Neo4jRelationshipType.CHILD
                )
            )

            // 父亲关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeId = nowNodeId,
                    endNodeId = nowNode.parentNode!!.id,
                    type = Neo4jRelationshipType.PARENT
                )
            )
        }


        if (nowNode.preNode != null) {
            // 前序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeId = nowNodeId,
                    endNodeId = nowNode.preNode!!.id,
                    type = Neo4jRelationshipType.PRE_SEQUENCE
                )
            )

            // 后序关系
            queryRunner.insertRelationship(
                Neo4jRelationship(
                    startNodeId = nowNode.preNode!!.id,
                    endNodeId = nowNodeId,
                    type = Neo4jRelationshipType.NEXT_SEQUENCE
                )
            )
        }

        // 文件包含关系
        queryRunner.insertRelationship(
            Neo4jRelationship(
                startNodeId = fileNode.id,
                endNodeId = nowNodeId,
                type = Neo4jRelationshipType.CONTAIN
            )
        )
    }
}
