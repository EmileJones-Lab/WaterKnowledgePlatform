package top.emilejones.hhu.textsplitter.repository.neo4j

import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode

/**
 * Neo4j 查询仓库接口
 * 负责基于 ID 或属性的基本查找和检索操作
 *
 * @author EmileJones
 */
interface INeo4jQueryRepository {

    /**
     * 根据节点ID查询文件节点
     *
     * @param id 节点ID
     * @return 对应的文件节点，如果不存在返回null
     */
    fun searchNeo4jFileNodeByNodeId(id: String): Neo4jFileNode?

    /**
     * 根据文件ID查询文件节点
     *
     * @param fileId 文件ID
     * @return 对应的文件节点，如果不存在返回null
     */
    fun searchNeo4jFileNodeByFileId(fileId: String): Neo4jFileNode?

    /**
     * 根据文件ID查询文本节点列表
     *
     * @param fileId 文件ID
     * @return 文本节点列表
     */
    fun searchNeo4jTextNodeByFileId(fileId: String): MutableList<Neo4jTextNode>

    /**
     * 根据节点ID查询文本节点
     *
     * @param id 节点ID
     * @return 对应的文本节点，如果不存在返回null
     */
    fun searchNeo4jTextNodeByNodeId(id: String): Neo4jTextNode?

    /**
     * 根据文本节点ID查询所属文件节点
     *
     * @param id 文本节点ID
     * @return 所属的文件节点
     */
    fun searchNeo4jFileNodeByTextNode(id: String): Neo4jFileNode

    /**
     * 根据节点ID列表批量查询文本节点
     *
     * @param idList 节点ID列表
     * @return 文本节点列表
     */
    fun batchSearchNeo4jTextNodeByNodeId(idList: List<String>): List<Neo4jTextNode>
}
