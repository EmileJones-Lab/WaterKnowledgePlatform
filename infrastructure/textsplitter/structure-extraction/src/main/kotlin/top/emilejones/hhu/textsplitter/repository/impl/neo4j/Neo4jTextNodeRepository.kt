package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.asNeo4jTextNode
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertTextNode

@Repository
class Neo4jTextNodeRepository(
    private val driver: Driver,
    private val neo4jConfig: Neo4jConfig
) {
    /**
     * 插入新的文本节点。
     * 执行逻辑：开启 Neo4j 会话，通过扩展方法执行 `CREATE` 语句持久化文本节点属性。
     * 
     * @param node 文本节点对象
     * @return 插入后的文本节点
     */
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertTextNode(node)
        }
    }

    /**
     * 根据业务文件 ID 获取所有关联的文本节点。
     * 执行逻辑：查询指定 `FileNode` 通过 `CONTAIN` 关系连接的所有 `TextNode`。
     * 
     * @param fileId 业务定义的文件 ID 或文件节点 ID
     * @return 文本节点的可变列表
     */
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

    /**
     * 根据节点 ID 查询文本节点。
     * 执行逻辑：匹配指定 `id` 且未标记删除的 `TextNode`。
     * 
     * @param id 节点唯一标识
     * @return 文本节点对象，未找到则返回 null
     */
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

    /**
     * 批量查询文本节点。
     * 执行逻辑：使用 `IN` 关键字匹配 ID 列表中的所有活跃 `TextNode`。
     * 
     * @param idList 节点 ID 列表
     * @return 查找到的文本节点列表
     */
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

    /**
     * 软删除指定的文本节点。
     * 执行逻辑：通过 `SET` 语句更新目标节点的 `isDelete` 状态。
     * 
     * @param id 文本节点 ID
     */
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

    /**
     * 批量软删除指定文件下的所有文本节点。
     * 执行逻辑：匹配文件节点连接的所有文本节点，并批量更新其 `isDelete` 属性。
     * 
     * @param fileId 业务文件 ID
     */
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

    /**
     * 硬删除文本节点及其所有关系（DETACH DELETE）。
     * 执行逻辑：通过 `DETACH DELETE` 物理移除文本节点。
     * 
     * @param id 文本节点 ID
     */
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
