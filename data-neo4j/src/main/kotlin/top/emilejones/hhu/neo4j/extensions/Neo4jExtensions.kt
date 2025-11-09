package top.emilejones.hhu.neo4j.extensions

import org.neo4j.driver.QueryRunner
import org.neo4j.driver.Values
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.Relationship
import top.emilejones.hhu.domain.enums.Neo4jRelationshipType
import top.emilejones.hhu.domain.enums.TextType
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode

fun Node.asNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        elementId = this.elementId(),
        text = this["text"].asString(),
        seq = this["seq"].asInt(),
        level = this["level"].asInt(),
        type = TextType.valueOf(this["type"].asString())
    )
}

fun Node.asNeo4jFileNode(): Neo4jFileNode {
    return Neo4jFileNode(
        elementId = this.elementId(),
        fileName = this["fileName"].asString(),
        isEmbedded = this["isEmbedded"].asBoolean()
    )
}

fun Relationship.asNeo4jRelationship(): Neo4jRelationship {
    return Neo4jRelationship(
        elementId = this.elementId(),
        startNodeElementId = this.startNodeElementId(),
        endNodeElementId = this.endNodeElementId(),
        type = Neo4jRelationshipType.valueOf(this.type())
    )
}

fun QueryRunner.insertTextNode(neo4jTextNode: Neo4jTextNode): Neo4jTextNode {
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

fun QueryRunner.insertFileNode(neo4jFileNode: Neo4jFileNode): Neo4jFileNode {
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

fun QueryRunner.insertRelationship(neo4jRelationship: Neo4jRelationship): Neo4jRelationship {
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