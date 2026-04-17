package top.emilejones.hhu.textsplitter.repository.impl.neo4j

import org.neo4j.driver.Driver
import org.neo4j.driver.SessionConfig
import org.springframework.stereotype.Repository
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode
import top.emilejones.hhu.infrastructure.configuration.env.pojo.Neo4jConfig
import top.emilejones.hhu.textsplitter.repository.INeo4jRepository
import top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions.insertRelationship

@Repository
class Neo4jRepositoryImpl(
    private val driver: Driver, private val neo4jConfig: Neo4jConfig,
    private val neo4jContextRepository: Neo4jContextRepository,
    private val neo4jFileNodeRepository: Neo4jFileNodeRepository,
    private val neo4jTextNodeRepository: Neo4jTextNodeRepository,
    private val nodeTreeRepository: NodeTreeRepository
) : INeo4jRepository {

    /**
     * 插入新的文本节点。
     * 
     * @param node 文本节点对象
     * @return 插入后的文本节点
     */
    override fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode {
        return neo4jTextNodeRepository.insertNeo4jTextNode(node)
    }

    /**
     * 插入新的文件节点。
     * 
     * @param node 文件节点对象
     * @return 插入后的文件节点
     */
    override fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode {
        return neo4jFileNodeRepository.insertNeo4jFileNode(node)
    }

    /**
     * 插入节点间的关系。
     * 
     * @param relationship 关系对象
     * @return 插入后的关系
     */
    override fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship {
        return driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.insertRelationship(relationship)
        }
    }

    /**
     * 根据节点 ID 查询文件节点。
     * 
     * @param id 节点唯一标识
     * @return 文件节点，不存在则返回 null
     */
    override fun searchNeo4jFileNodeByNodeId(id: String): Neo4jFileNode? {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByNodeId(id)
    }

    /**
     * 以树状结构递归插入文本节点及其关系。
     * 
     * @param rootNode 树的虚拟根节点（不持久化，其子节点开始持久化）
     */
    override fun insertTree(rootNode: TextNodeDTO) {
        nodeTreeRepository.insertTree(rootNode)
    }

    /**
     * 根据文件节点 ID 构建并返回完整的结构化树。
     * 
     * @param fileNodeId 文件节点唯一标识
     * @return 包含层级结构的虚拟根节点
     */
    override fun findTreeByFileNodeId(fileNodeId: String): TextNodeDTO {
        return nodeTreeRepository.findTreeByFileNodeId(fileNodeId)
    }

    /**
     * 根据文件 ID 查询文件节点。
     * 
     * @param fileId 业务文件 ID
     * @return 文件节点，不存在则返回 null
     */
    override fun searchNeo4jFileNodeByFileId(fileId: String): Neo4jFileNode? {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByFileId(fileId)
    }

    /**
     * 获取指定文件下的所有文本节点。
     * 
     * @param fileId 业务文件 ID
     * @return 文本节点列表
     */
    override fun searchNeo4jTextNodeByFileId(fileId: String): MutableList<Neo4jTextNode> {
        return neo4jTextNodeRepository.searchTextNodeByFileId(fileId)
    }

    /**
     * 根据节点 ID 查询文本节点。
     * 
     * @param id 节点唯一标识
     * @return 文本节点，不存在则返回 null
     */
    override fun searchNeo4jTextNodeByNodeId(id: String): Neo4jTextNode? {
        return neo4jTextNodeRepository.searchTextNodeByNodeId(id)
    }

    /**
     * 根据文本节点 ID 查询其所属的文件节点。
     * 
     * @param id 文本节点唯一标识
     * @return 所属的文件节点
     */
    override fun searchNeo4jFileNodeByTextNode(id: String): Neo4jFileNode {
        return neo4jFileNodeRepository.searchNeo4jFileNodeByTextNode(id)
    }

    /**
     * 批量查询文本节点。
     * 
     * @param idList 节点 ID 列表
     * @return 查找到的文本节点列表
     */
    override fun batchSearchNeo4jTextNodeByNodeId(idList: List<String>): List<Neo4jTextNode> {
        return neo4jTextNodeRepository.batchSearchTextNodeByNodeId(idList)
    }

    /**
     * 更新指定节点的属性。
     * 
     * @param id 节点唯一标识
     * @param needUpdatedAttr 待更新或新增的属性键值对
     */
    override fun updateNodeById(id: String, needUpdatedAttr: Map<String, Any?>) {
        val setCypher = needUpdatedAttr.keys.map { "n.${it} = ${'$'}${it}" }
            .joinToString(separator = ", \n", postfix = "\n", prefix = "SET ")
        val cypher = """
            MATCH (n)
            WHERE n.id = ${'$'}id
            $setCypher
        """.trimIndent()
        val params = mutableMapOf<String, Any?>(Pair("id", id)) + needUpdatedAttr
        driver.session(SessionConfig.forDatabase(neo4jConfig.database)).use {
            it.run(cypher, params)
        }
    }

    /**
     * 软删除文本节点（标记删除）。
     * 
     * @param id 文本节点唯一标识
     */
    override fun deleteTextNodeById(id: String) {
        neo4jTextNodeRepository.softDeleteTextNodeById(id)
    }

    /**
     * 硬删除文本节点及其所有关系。
     * 
     * @param id 文本节点唯一标识
     */
    override fun hardDeleteTextNodeById(id: String) {
        neo4jTextNodeRepository.hardDeleteTextNodeById(id)
    }

    /**
     * 软删除文件节点及其包含的所有文本节点。
     * 
     * @param id 文件节点唯一标识
     */
    override fun deleteFileNodeById(id: String) {
        neo4jFileNodeRepository.softDeleteFileNodeById(id)
    }

    /**
     * 硬删除文件节点及其包含的所有内容和关系。
     * 
     * @param id 文件节点唯一标识
     */
    override fun hardDeleteFileNodeById(id: String) {
        neo4jFileNodeRepository.hardDeleteFileNodeById(id)
    }

    /**
     * 获取下一个序列节点。
     * 
     * @param id 当前节点唯一标识
     * @return 下一个节点的文件及文本信息，不存在则返回 null
     */
    override fun nextNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        return neo4jContextRepository.nextNode(id)
    }

    /**
     * 获取前一个序列节点。
     * 
     * @param id 当前节点唯一标识
     * @return 前一个节点的文件及文本信息，不存在则返回 null
     */
    override fun preNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>? {
        return neo4jContextRepository.preNode(id)
    }

    /**
     * 获取当前节点的父级文本节点。
     * 
     * @param id 当前节点唯一标识
     * @return 父节点信息，不存在则返回 null
     */
    override fun parent(id: String): Neo4jTextNode? {
        return neo4jContextRepository.parent(id)
    }

    /**
     * 获取当前节点的所有兄弟节点（同父级且按顺序排列）。
     * 
     * @param id 当前节点唯一标识
     * @return 兄弟节点列表
     */
    override fun siblings(id: String): List<Neo4jTextNode> {
        return neo4jContextRepository.siblings(id)
    }

    /**
     * 获取当前节点的所有子节点（按顺序排列）。
     * 
     * @param id 当前节点唯一标识
     * @return 子节点列表
     */
    override fun children(id: String): List<Neo4jTextNode> {
        return neo4jContextRepository.children(id)
    }


    /**
     * 清空数据库中所有的文本节点和文件节点。
     */
    override fun clearAllData() {
        driver.session().use { session ->
            session.executeWriteWithoutResult {
                it.run("MATCH (n: TextNode | FileNode) DETACH DELETE n")
            }
        }
    }
}
