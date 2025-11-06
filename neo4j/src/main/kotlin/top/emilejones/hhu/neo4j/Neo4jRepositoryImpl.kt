package top.emilejones.hhu.neo4j

import org.neo4j.driver.*
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.Relationship
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.dto.FileNode
import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.enums.Neo4jRelationshipType
import top.emilejones.hhu.domain.enums.TextType
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode
import top.emilejones.hhu.repository.INeo4jRepository
import java.util.*
import kotlin.reflect.KProperty

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

    override fun nextNode(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use {
            val record: Record = it.run(
                cypher,
                params
            ).single()
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    override fun preNode(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE elementId(n) = ${'$'}elementId
                RETURN m, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("elementId", elementId)

        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            val record = session.run(
                cypher,
                params
            ).single()
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    override fun selectByElementId(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>? {
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

    private fun QueryRunner.insertTextNode(neo4jTextNode: Neo4jTextNode): Neo4jTextNode {
        logger.trace(
            "Start insert TextNode, textLength:[{}], sequence: [{}]",
            neo4jTextNode.length,
            neo4jTextNode.seq
        )
        val insertTextNodeResult = this.run(
            """
            CREATE (n:TextNode {
                text: ${'$'}text,
                seq: ${'$'}seq,
                level: ${'$'}level,
                name: ${'$'}name,
                length: ${'$'}length,
                type: ${'$'}type
            })
            RETURN n
        """,
            Values.parameters(
                "text", neo4jTextNode.text,
                "seq", neo4jTextNode.seq,
                "level", neo4jTextNode.level,
                "name", neo4jTextNode.seq,
                "length", neo4jTextNode.length,
                "type", neo4jTextNode.type.name
            )
        ).single()
        return insertTextNodeResult["n"].asNode().asNeo4jTextNode()
    }

    private fun QueryRunner.insertFileNode(neo4jFileNode: Neo4jFileNode): Neo4jFileNode {
        logger.trace("Start insert FileNode, fileName: [{}]", neo4jFileNode.fileName)
        val insertFileNodeResult = this.run(
            """
            CREATE (n:FileNode {
                fileName: ${'$'}fileName,
                isEmbedded: ${'$'}isEmbedded
            })
            RETURN n
        """,
            Values.parameters(
                "fileName", neo4jFileNode.fileName,
                "isEmbedded", neo4jFileNode.isEmbedded
            )
        ).single()
        return insertFileNodeResult["n"].asNode().asNeo4jFileNode()
    }

    private fun QueryRunner.insertRelationship(neo4jRelationship: Neo4jRelationship): Neo4jRelationship {
        logger.trace(
            "Start insert neo4j relationship, relationship type: [{}], startElementId: [{}], endElementId: [{}]",
            neo4jRelationship.type,
            neo4jRelationship.startNodeElementId,
            neo4jRelationship.endNodeElementId
        )
        val insertRelationshipResult = this.run(
            """
            MATCH (startNode)
            WHERE elementId(startNode) = "%s" 
            MATCH (endNode)
            WHERE elementId(endNode) = "%s"
            CREATE (startNode)-[r:`%s`]->(endNode)
            RETURN r
        """.trimIndent().format(
                neo4jRelationship.startNodeElementId,
                neo4jRelationship.endNodeElementId,
                neo4jRelationship.type.name
            )
        ).single()
        return insertRelationshipResult["r"].asRelationship().asNeo4jRelationship()
    }

    private fun TextNode.toNeo4jTextNode(): Neo4jTextNode {
        return Neo4jTextNode(
            text = this.text,
            seq = this.seq,
            level = this.level,
            type = this.type
        )
    }

    private fun FileNode.toNeo4jFileNode(): Neo4jFileNode {
        return Neo4jFileNode(
            fileName = this.fileName,
            isEmbedded = false
        )
    }

    private fun Node.asNeo4jTextNode(): Neo4jTextNode {
        return Neo4jTextNode(
            elementId = this.elementId(),
            text = this["text"].asString(),
            seq = this["seq"].asInt(),
            level = this["level"].asInt(),
            type = TextType.valueOf(this["type"].asString())
        )
    }

    private fun Node.asNeo4jFileNode(): Neo4jFileNode {
        return Neo4jFileNode(
            elementId = this.elementId(),
            fileName = this["fileName"].asString(),
            isEmbedded = this["isEmbedded"].asBoolean()
        )
    }

    private fun Relationship.asNeo4jRelationship(): Neo4jRelationship {
        return Neo4jRelationship(
            elementId = this.elementId(),
            startNodeElementId = this.startNodeElementId(),
            endNodeElementId = this.endNodeElementId(),
            type = Neo4jRelationshipType.valueOf(this.type())
        )
    }
}

/**
 * 用来绑定每一个TextNode和FileNode对应的elementId
 */
private class TextNodeDelegate {
    private val cache = WeakHashMap<TextNode, String?>()

    operator fun getValue(thisRef: TextNode, property: KProperty<*>): String? {
        return cache.getOrPut(thisRef) { null }
    }

    operator fun setValue(thisRef: TextNode, property: KProperty<*>, value: String?) {
        cache[thisRef] = value
    }
}

private var TextNode.elementId: String? by TextNodeDelegate()