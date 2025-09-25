package top.emilejones.hhu.repository.neo4j.impl

import org.neo4j.driver.*
import org.neo4j.driver.Values.parameters
import org.slf4j.LoggerFactory
import top.emilejones.hhu.domain.FileNode
import top.emilejones.hhu.domain.TextNode
import top.emilejones.hhu.repository.neo4j.INeo4jRepository
import top.emilejones.hhu.repository.neo4j.enums.Neo4jRelationshipType
import top.emilejones.hhu.repository.neo4j.enums.TextType
import top.emilejones.hhu.repository.neo4j.po.Neo4jFileNode
import top.emilejones.hhu.repository.neo4j.po.Neo4jRelationship
import top.emilejones.hhu.repository.neo4j.po.Neo4jTextNode
import java.util.*
import kotlin.reflect.KProperty

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
        for (i in 0..<rootNode.childNum()){
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
                    RETURN t as n
                """.trimIndent()

                val result = tx.run(
                    query, parameters(
                        "filename", filename
                    )
                )

                result.list().map { it.asNeo4jTextNode() }.toMutableList()
            }
        }
    }

    override fun searchFileNodeByFileName(filename: String): Neo4jFileNode? {
        driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:FileNode)
                    WHERE n.fileName = ${'$'}filename
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, parameters(
                        "filename", filename
                    )
                )

                if (!result.hasNext())
                    null
                else
                    result.single().asNeo4jFileNode()
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
            parameters(
                "text", neo4jTextNode.text,
                "seq", neo4jTextNode.seq,
                "level", neo4jTextNode.level,
                "name", neo4jTextNode.seq,
                "length", neo4jTextNode.length,
                "type", neo4jTextNode.type.name
            )
        ).single()
        return insertTextNodeResult.asNeo4jTextNode()
    }

    private fun QueryRunner.insertFileNode(neo4jTextNode: Neo4jFileNode): Neo4jFileNode {
        logger.trace("Start insert FileNode, fileName: [{}]", neo4jTextNode.fileName)
        val insertFileNodeResult = this.run(
            """
            CREATE (n:FileNode {
                fileName: ${'$'}fileName
            })
            RETURN n
        """,
            parameters(
                "fileName", neo4jTextNode.fileName
            )
        ).single()
        return insertFileNodeResult.asNeo4jFileNode()
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
        return insertRelationshipResult.asNeo4jRelationship()
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
            fileName = this.fileName
        )
    }

    private fun Record.asNeo4jTextNode(): Neo4jTextNode {
        val node = this["n"].asNode()
        return Neo4jTextNode(
            elementId = node.elementId(),
            text = node["text"].asString(),
            seq = node["seq"].asInt(),
            level = node["level"].asInt(),
            type = TextType.valueOf(node["type"].asString())
        )
    }

    private fun Record.asNeo4jFileNode(): Neo4jFileNode {
        val node = this["n"].asNode()
        return Neo4jFileNode(
            elementId = node.elementId(),
            fileName = node["fileName"].asString()
        )
    }

    private fun Record.asNeo4jRelationship(): Neo4jRelationship {
        val relationship = this["r"].asRelationship()
        return Neo4jRelationship(
            elementId = relationship.elementId(),
            startNodeElementId = relationship.startNodeElementId(),
            endNodeElementId = relationship.endNodeElementId(),
            type = Neo4jRelationshipType.valueOf(relationship.type())
        )
    }
}