package top.emilejones.hhu.repository.impl.neo4j

import org.neo4j.driver.*
import org.neo4j.driver.types.Node
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.enums.Neo4jRelationshipType
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode
import top.emilejones.hhu.repository.impl.neo4j.delegates.elementId
import top.emilejones.hhu.repository.INeo4jRepository
import top.emilejones.hhu.repository.impl.neo4j.extensions.*

class Neo4jRepositoryImpl(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val databaseName: String
) : INeo4jRepository {

    private val driver: Driver =
        GraphDatabase.driver("bolt://$host:$port", AuthTokens.basic(username, password))

    private val logger = LoggerFactory.getLogger(Neo4jRepositoryImpl::class.java)

    override fun close() {
        driver.close()
    }

    override fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode {
        return driver.session(SessionConfig.forDatabase(databaseName)).use {
            it.insertTextNode(node)
        }
    }

    override fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode {
        return driver.session(SessionConfig.forDatabase(databaseName)).use {
            it.insertFileNode(node)
        }
    }

    override fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship {
        return driver.session(SessionConfig.forDatabase(databaseName)).use {
            it.insertRelationship(relationship)
        }
    }

    override fun insertTree(rootNode: TextNode) {
        if (rootNode.fileNode == null)
            throw IllegalArgumentException("The fileNode of rootNode must be not null!")

        rootNode.getChild(0).preNode = null
        for (i in 0..<rootNode.childNum()) {
            rootNode.getChild(i).parentNode = null
        }

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
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
                        logger.debug("Success insert [{}] Tree, transaction commited.", rootNode.fileNode!!.fileName)
                    },
                    onFailure = {
                        transaction.rollback()
                        logger.error("Taking an exception when insert ${rootNode.fileNode!!.fileName} Tree", it)
                        throw RuntimeException(it)
                    }
                )

            }
        }
    }

    override fun searchNeo4jTextNodeByFilename(filename: String): MutableList<Neo4jTextNode> {
        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (f:FileNode)-[:CONTAIN]->(t:TextNode)
                    WHERE f.fileName = ${'$'}filename
                    RETURN t
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "filename", filename
                    )
                )

                result.list().map { it["n"].asNode().asNeo4jTextNode() }.toMutableList()
            }
        }
    }

    override fun searchNeo4jFileNodeByFileName(filename: String): Neo4jFileNode? {
        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:FileNode)
                    WHERE n.fileName = ${'$'}filename
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "filename", filename
                    )
                )

                if (!result.hasNext())
                    null
                else
                    result.single()["n"].asNode().asNeo4jFileNode()
            }
        }
    }

    override fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any>) {
        val setCypher = needUpdatedAttr.keys.map { "n.${it} = ${'$'}${it}" }
            .joinToString(separator = ", \n", postfix = "\n", prefix = "SET ")
        val cypher = """
            MATCH (n)
            WHERE elementId(n) = ${'$'}elementId
            ${setCypher}
        """.trimIndent()
        val params = mutableMapOf<String, Any>(Pair("elementId", elementId)) + needUpdatedAttr
        driver.session(SessionConfig.forDatabase(databaseName)).use {
            it.run(cypher, params)
        }
    }

    override fun nextNode(elementId: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use {
            val record: Record? = kotlin.runCatching {
                it.run(
                    cypher,
                    params
                ).single()
            }.getOrNull()
            if (record == null) return null
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    override fun preNode(elementId: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val record = runCatching {
                session.run(
                    cypher,
                    params
                ).single()
            }.getOrNull()
            if (record == null) return null
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    override fun selectByElementId(elementId: String): Pair<Neo4jFileNode, Neo4jTextNode> {
        val cypher = """
                MATCH (n:TextNode)<-[r:CONTAIN]-(f:FileNode)
                WHERE elementId(n) = ${'$'}elementId
                RETURN n, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val record = session.run(
                cypher,
                params
            ).single()
            val textNode = record["n"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    override fun parent(elementId: String): Neo4jTextNode? {
        val cypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val result = session.run(
                cypher,
                params
            )
            if (!result.hasNext()) return null
            val record = result.single()
            val m = record["m"].asNode()
            return m.asNeo4jTextNode()
        }
    }

    override fun siblings(elementId: String): List<Neo4jTextNode> {
        val findParentCypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m
                
                """.trimIndent()
        val findChildCypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m
                ORDER BY m.seq ASC;
                
                """.trimIndent()


        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val findParentParams = java.util.Map.of<String, Any>("elementId", elementId)
            val parentResult = session.run(
                findParentCypher,
                findParentParams
            )
            // 如果不存在父亲节点，则返回空
            if (!parentResult.hasNext()) {
                return ArrayList()
            }
            // 如果存在父亲节点则查找父亲节点的孩子，就是此节点的兄弟
            val record = parentResult.single()
            val m = record["m"].asNode()
            val parentNode = m.asNeo4jTextNode()

            val findChildrenParams = java.util.Map.of<String, Any>("elementId", parentNode.elementId)
            val childrenResult = session.run(
                findChildCypher,
                findChildrenParams
            )
            return childrenResult.list().stream()
                .map { r: Record -> r["m"].asNode() }
                .map { node: Node -> node.asNeo4jTextNode() }
                .toList()
        }
    }

    override fun children(elementId: String): List<Neo4jTextNode> {
        val cypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m
                ORDER BY m.seq ASC;
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val result = session.run(
                cypher,
                params
            )
            val textNodes = java.util.ArrayList<Neo4jTextNode>()
            for (record in result.list()) {
                val m = record["m"].asNode()
                textNodes.add(m.asNeo4jTextNode())
            }
            return textNodes
        }
    }

    override fun clearAllData() {
        driver.session().use { session ->
            session.executeWriteWithoutResult {
                it.run("MATCH (n: TextNode | FileNode) DETACH DELETE n")
            }
        }
    }

    private fun deepVisitAndInsertNode(
        fileNode: Neo4jFileNode,
        nowNode: TextNode,
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
        nowNode: TextNode,
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

