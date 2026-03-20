package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jTextNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertTextNode

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
                    WHERE (f.fileId = ${'$'}fileId OR f.id = ${'$'}fileId) AND coalesce(f.isDelete, false) = false AND coalesce(t.isDelete, false) = false
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
                    WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "id", id
                    )
                )
                if (!result.hasNext()) null else result.single()["n"].asNode().asNeo4jTextNode()
            }
        }
    }

    fun batchSearchTextNodeByNodeId(idList: List<String>): List<Neo4jTextNode> {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:TextNode)
                    WHERE n.id IN ${'$'}idList AND coalesce(n.isDelete, false) = false
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "idList", idList
                    )
                )

                result.list().map { it["n"].asNode().asNeo4jTextNode() }
            }
        }
    }

    fun softDeleteTextNodeById(id: String) {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.executeWriteWithoutResult {
                it.run(
                    """
                        MATCH (n:TextNode)
                        WHERE n.id = ${'$'}id
                        SET n.isDelete = true
                    """.trimIndent(),
                    Values.parameters("id", id)
                )
            }
        }
    }

    fun softDeleteTextNodesByFileId(fileId: String) {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.executeWriteWithoutResult {
                it.run(
                    """
                        MATCH (f:FileNode)-[:CONTAIN]->(n:TextNode)
                        WHERE (f.fileId = ${'$'}fileId OR f.id = ${'$'}fileId)
                          AND coalesce(f.isDelete, false) = false
                          AND coalesce(n.isDelete, false) = false
                        SET n.isDelete = true
                    """.trimIndent(),
                    Values.parameters("fileId", fileId)
                )
            }
        }
    }

    fun hardDeleteTextNodeById(id: String) {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.executeWriteWithoutResult {
                it.run(
                    """
                        MATCH (n:TextNode)
                        WHERE n.id = ${'$'}id
                        DETACH DELETE n
                    """.trimIndent(),
                    Values.parameters("id", id)
                )
            }
        }
    }
}
