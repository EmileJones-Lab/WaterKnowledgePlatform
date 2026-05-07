package top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions

import org.neo4j.driver.QueryRunner
import org.neo4j.driver.Values
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.Relationship
import top.emilejones.hhu.domain.result.TextType
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jRelationshipType
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode

/**
 * 将 Neo4j 驱动返回的 Node 对象转换为 Neo4jTextNode PO。
 * 执行逻辑：从 Node 属性中提取文本、序列、类型等字段，处理可能的 null 向量。
 */
fun Node.asNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        text = this["text"].asString(),
        summary = this["summary"].takeUnless { it.isNull }?.asString(),
        seq = this["seq"].asInt(),
        level = this["level"].asInt(),
        type = TextType.valueOf(this["type"].asString()),
        vector = this["vector"].takeUnless { it.isNull }?.asList { it.asFloat() },
        id = this["id"].asString(),
        isDelete = if (this.keys().contains("isDelete")) this["isDelete"].asBoolean() else false
    )
}

/**
 * 将 Neo4j 驱动返回的 Node 对象转换为 Neo4jFileNode PO。
 * 执行逻辑：从 Node 属性中映射文件标识及状态位。
 */
fun Node.asNeo4jFileNode(): Neo4jFileNode {
    return Neo4jFileNode(
        fileId = this["fileId"].asString(),
        isEmbedded = this["isEmbedded"].asBoolean(),
        id = this["id"].asString(),
        isDelete = if (this.keys().contains("isDelete")) this["isDelete"].asBoolean() else false,
        fileAbstract = this["fileAbstract"].takeUnless { it.isNull }?.asString(),
        vector = this["vector"].takeUnless { it.isNull }?.asList { it.asFloat() }
    )
}

/**
 * 将 Neo4j 驱动返回的 Relationship 对象转换为 Neo4jRelationship PO。
 * 执行逻辑：映射起始/结束节点 ID 及关系类型。
 */
fun Relationship.asNeo4jRelationship(): Neo4jRelationship {
    return Neo4jRelationship(
        startNodeId = this.startNodeElementId(), // 这里的驱动方法名虽然叫 elementId，但在本场景下我们通过属性映射处理
        endNodeId = this.endNodeElementId(),
        type = Neo4jRelationshipType.valueOf(this.type())
    )
}

/**
 * 持久化文本节点。
 * 执行逻辑：执行 `CREATE (n:TextNode ...)` Cypher 语句，并返回持久化后的节点。
 */
fun QueryRunner.insertTextNode(neo4jTextNode: Neo4jTextNode): Neo4jTextNode {
    val insertTextNodeResult = this.run(
        """
            CREATE (n:TextNode {
                id: ${'$'}id,
                text: ${'$'}text,
                summary: ${'$'}summary,
                seq: ${'$'}seq,
                level: ${'$'}level,
                name: ${'$'}name,
                length: ${'$'}length,
                type: ${'$'}type,
                vector: ${'$'}vector,
                isDelete: ${'$'}isDelete
            })
            RETURN n
        """,
        Values.parameters(
            "text", neo4jTextNode.text,
            "summary", neo4jTextNode.summary,
            "seq", neo4jTextNode.seq,
            "level", neo4jTextNode.level,
            "name", neo4jTextNode.seq,
            "length", neo4jTextNode.length,
            "type", neo4jTextNode.type.name,
            "vector", neo4jTextNode.vector,
            "id", neo4jTextNode.id,
            "isDelete", neo4jTextNode.isDelete
        )
    ).single()
    return insertTextNodeResult["n"].asNode().asNeo4jTextNode()
}

/**
 * 持久化文件节点。
 * 执行逻辑：执行 `CREATE (n:FileNode ...)` Cypher 语句，并返回持久化后的节点。
 */
fun QueryRunner.insertFileNode(neo4jFileNode: Neo4jFileNode): Neo4jFileNode {
    val insertFileNodeResult = this.run(
        """
            CREATE (n:FileNode {
                id: ${'$'}id,
                name: ${'$'}fileId,
                fileId: ${'$'}fileId,
                isEmbedded: ${'$'}isEmbedded,
                isDelete: ${'$'}isDelete,
                fileAbstract: ${'$'}fileAbstract,
                vector: ${'$'}vector
            })
            RETURN n
        """,
        Values.parameters(
            "fileId", neo4jFileNode.fileId,
            "name", neo4jFileNode.fileId,
            "isEmbedded", neo4jFileNode.isEmbedded,
            "id", neo4jFileNode.id,
            "isDelete", neo4jFileNode.isDelete,
            "fileAbstract", neo4jFileNode.fileAbstract,
            "vector", neo4jFileNode.vector
        )
    ).single()
    return insertFileNodeResult["n"].asNode().asNeo4jFileNode()
}

/**
 * 持久化节点间的关系。
 * 执行逻辑：通过 `MATCH` 查找起止节点，随后执行 `CREATE (startNode)-[r:...]->(endNode)` 建立关系。
 */
fun QueryRunner.insertRelationship(neo4jRelationship: Neo4jRelationship): Neo4jRelationship {
    val query = when (neo4jRelationship.type) {
        Neo4jRelationshipType.CHILD -> """
            MATCH (startNode {id: ${'$'}startNodeId})
            MATCH (endNode {id: ${'$'}endNodeId})
            CREATE (startNode)-[r:CHILD]->(endNode)
            RETURN r
        """.trimIndent()
        Neo4jRelationshipType.PARENT -> """
            MATCH (startNode {id: ${'$'}startNodeId})
            MATCH (endNode {id: ${'$'}endNodeId})
            CREATE (startNode)-[r:PARENT]->(endNode)
            RETURN r
        """.trimIndent()
        Neo4jRelationshipType.PRE_SEQUENCE -> """
            MATCH (startNode {id: ${'$'}startNodeId})
            MATCH (endNode {id: ${'$'}endNodeId})
            CREATE (startNode)-[r:PRE_SEQUENCE]->(endNode)
            RETURN r
        """.trimIndent()
        Neo4jRelationshipType.NEXT_SEQUENCE -> """
            MATCH (startNode {id: ${'$'}startNodeId})
            MATCH (endNode {id: ${'$'}endNodeId})
            CREATE (startNode)-[r:NEXT_SEQUENCE]->(endNode)
            RETURN r
        """.trimIndent()
        Neo4jRelationshipType.CONTAIN -> """
            MATCH (startNode {id: ${'$'}startNodeId})
            MATCH (endNode {id: ${'$'}endNodeId})
            CREATE (startNode)-[r:CONTAIN]->(endNode)
            RETURN r
        """.trimIndent()
    }
    val insertRelationshipResult = this.run(
        query,
        Values.parameters(
            "startNodeId", neo4jRelationship.startNodeId,
            "endNodeId", neo4jRelationship.endNodeId
        )
    ).single()
    return insertRelationshipResult["r"].asRelationship().asNeo4jRelationship()
}
