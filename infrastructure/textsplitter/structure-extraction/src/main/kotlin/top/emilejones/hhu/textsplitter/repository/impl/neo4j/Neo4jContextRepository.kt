package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.Record
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.types.Node
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jFileNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jTextNode

@Repository
class Neo4jContextRepository(
    private val driver: Driver, private val neo4jConfig: Neo4jConfig
) {

    fun nextNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`NEXT_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false AND coalesce(f.isDelete, false) = false
                RETURN m, f
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("id", id)

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            val record: Record? = kotlin.runCatching {
                it.run(
                    cypher, params
                ).single()
            }.getOrNull()
            if (record == null) return null
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    fun preNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        val cypher = """
                MATCH (n:TextNode)-[r:`PRE_SEQUENCE`]->(m:TextNode)
                MATCH (f)-[:CONTAIN]->(m)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false AND coalesce(f.isDelete, false) = false
                RETURN m, f
                
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("id", id)

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            val record = runCatching {
                session.run(
                    cypher, params
                ).single()
            }.getOrNull()
            if (record == null) return null
            val textNode = record["m"].asNode()
            val fileNode = record["f"].asNode()
            return Pair(fileNode.asNeo4jFileNode(), textNode.asNeo4jTextNode())
        }
    }

    fun parent(id: String): Neo4jTextNode? {
        val cypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false
                RETURN m
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("id", id)

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            val result = session.run(
                cypher, params
            )
            if (!result.hasNext()) return null
            val record = result.single()
            val m = record["m"].asNode()
            return m.asNeo4jTextNode()
        }
    }

    fun siblings(id: String): List<Neo4jTextNode> {
        val findParentCypher = """
                MATCH (n:TextNode)-[r:`PARENT`]->(m:TextNode)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false
                RETURN m
                
                """.trimIndent()
        val findChildCypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false
                RETURN m
                ORDER BY m.seq ASC;
                
                """.trimIndent()


        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            val findParentParams = java.util.Map.of<String, Any>("id", id)
            val parentResult = session.run(
                findParentCypher, findParentParams
            )
            // 如果不存在父亲节点，则返回空
            if (!parentResult.hasNext()) {
                return ArrayList()
            }
            // 如果存在父亲节点则查找父亲节点的孩子，就是此节点的兄弟
            val record = parentResult.single()
            val m = record["m"].asNode()
            val parentNode = m.asNeo4jTextNode()

            val findChildrenParams = java.util.Map.of<String, Any>("id", parentNode.id)
            val childrenResult = session.run(
                findChildCypher, findChildrenParams
            )
            return childrenResult.list().stream().map { r: Record -> r["m"].asNode() }
                .map { node: Node -> node.asNeo4jTextNode() }.toList()
        }
    }

    fun children(id: String): List<Neo4jTextNode> {
        val cypher = """
                MATCH (n:TextNode)-[r:`CHILD`]->(m:TextNode)
                WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false AND coalesce(m.isDelete, false) = false
                RETURN m
                ORDER BY m.seq ASC;
                """.trimIndent()

        val params = java.util.Map.of<String, Any>("id", id)

        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            val result = session.run(
                cypher, params
            )
            val textNodes = java.util.ArrayList<Neo4jTextNode>()
            for (record in result.list()) {
                val m = record["m"].asNode()
                textNodes.add(m.asNeo4jTextNode())
            }
            return textNodes
        }
    }
}
