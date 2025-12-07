package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jFileNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertFileNode

@Repository
class Neo4jFileNodeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {

    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertFileNode(node)
        }
    }

    fun searchNeo4jFileNodeByFileId(fileId: String): Neo4jFileNode? {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:FileNode)
                    WHERE n.fileId = ${'$'}fileId AND coalesce(n.isDelete, false) = false
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "fileId", fileId
                    )
                )

                if (!result.hasNext())
                    null
                else
                    result.single()["n"].asNode().asNeo4jFileNode()
            }
        }
    }

    fun searchNeo4jFileNodeByNodeId(id: String): Neo4jFileNode? {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (n:FileNode)
                    WHERE n.id = ${'$'}id AND coalesce(n.isDelete, false) = false
                    RETURN n
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "id", id
                    )
                )

                if (!result.hasNext())
                    null
                else
                    result.single()["n"].asNode().asNeo4jFileNode()
            }
        }
    }

    fun searchNeo4jFileNodeByTextNode(id: String): Neo4jFileNode {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            return session.executeRead { tx ->
                val query = """
                    MATCH (f:FileNode)-[:CONTAIN]->(t:TextNode)
                    WHERE t.id = ${'$'}id AND coalesce(f.isDelete, false) = false AND coalesce(t.isDelete, false) = false
                    RETURN f
                """.trimIndent()

                val result = tx.run(
                    query, Values.parameters(
                        "id", id
                    )
                )
                if (!result.hasNext()) {
                    throw NoSuchElementException("FileNode not found for text node [$id]")
                }
                result.single()["f"].asNode().asNeo4jFileNode()
            }
        }
    }

    fun softDeleteFileNodeById(id: String) {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.executeWriteWithoutResult {
                it.run(
                    """
                        MATCH (n:FileNode)
                        WHERE n.id = ${'$'}id
                        SET n.isDelete = true
                    """.trimIndent(),
                    Values.parameters("id", id)
                )
            }
        }
    }
}
