package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jFileNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertFileNode

@Repository
class Neo4jFileNodeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {

    /**
     * 插入新的文件节点。
     * 执行逻辑：开启 Neo4j 会话，调用扩展方法执行 `CREATE` 语句持久化文件节点。
     * 
     * @param node 文件节点对象
     * @return 插入后的文件节点
     */
    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertFileNode(node)
        }
    }

    /**
     * 根据业务文件 ID 查询文件节点。
     * 执行逻辑：在只读事务中运行 Cypher，匹配 `fileId` 匹配且 `isDelete` 为 false 的 `FileNode`。
     * 
     * @param fileId 业务定义的文件 ID
     * @return 文件节点对象，未找到则返回 null
     */
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

    /**
     * 根据节点唯一 ID 查询文件节点。
     * 执行逻辑：在只读事务中运行 Cypher，匹配 `id` 属性匹配且未删除的 `FileNode`。
     * 
     * @param id 节点唯一标识
     * @return 文件节点对象，未找到则返回 null
     */
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

    /**
     * 根据所属的文本节点查询文件节点。
     * 执行逻辑：通过 `CONTAIN` 关系反向查找指定 `TextNode` 所属的 `FileNode`。
     * 
     * @param id 文本节点 ID
     * @return 所属的文件节点
     * @throws NoSuchElementException 若未找到对应的文件节点
     */
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

    /**
     * 软删除文件节点。
     * 执行逻辑：执行 `SET` 语句将指定节点的 `isDelete` 属性置为 true。
     * 
     * @param id 文件节点唯一标识
     */
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

    /**
     * 硬删除文件节点及其关联关系（DETACH DELETE）。
     * 执行逻辑：使用 `DETACH DELETE` 语句物理删除节点及其所有关联的边。
     * 
     * @param id 文件节点唯一标识
     */
    fun hardDeleteFileNodeById(id: String) {
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use { session ->
            session.executeWriteWithoutResult {
                it.run(
                    """
                        MATCH (n:FileNode)
                        WHERE n.id = ${'$'}id
                        DETACH DELETE n
                    """.trimIndent(),
                    Values.parameters("id", id)
                )
            }
        }
    }
}
