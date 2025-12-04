package top.emilejones.hhu.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.springframework.stereotype.Repository
import top.emilejones.hhu.domain.po.Neo4jTextNode
import top.emilejones.hhu.env.pojo.Neo4jConfig
import top.emilejones.hhu.repository.impl.neo4j.extensions.asNeo4jTextNode
import top.emilejones.hhu.repository.impl.neo4j.extensions.insertTextNode

@Repository
class Neo4jTextNodeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertTextNode(node)
        }
    }

    fun searchTextNodeByFileId(fileId: String): MutableList<Neo4jTextNode> {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (f:FileNode)-[:CONTAIN]->(t:TextNode)
                    WHERE f.fileId = ${'$'}fileId
                    RETURN t
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "fileId", fileId
                    )
                )

                result.list().map { it["t"].asNode().asNeo4jTextNode() }.toMutableList()
            }
        }
    }

    fun searchTextNodeByNodeId(id: String): Neo4jTextNode? {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:TextNode)
                    WHERE n.id = ${'$'}id
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "id", id
                    )
                )
                result.single()["n"].asNode().asNeo4jTextNode()
            }
        }
    }
}