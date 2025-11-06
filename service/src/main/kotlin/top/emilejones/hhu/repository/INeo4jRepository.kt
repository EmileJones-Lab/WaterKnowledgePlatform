package top.emilejones.hhu.repository

import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode

/**
 * @author EmileJones
 */
interface INeo4jRepository : AutoCloseable {
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode
    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode
    fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship

    /**
     * 按照树状结构插入
     * @param rootNode 整个树状结构的根节点，这个根节点是空的头节点，它不会被插入。
     */
    fun insertTree(rootNode: TextNode)
    fun searchNeo4jTextNodeByFilename(filename: String): MutableList<Neo4jTextNode>
    fun searchNeo4jFileNodeByFileName(filename: String): Neo4jFileNode?

    /**
     * 根据elementId去修改节点的属性，如果属性不存在，则添加。
     * @param elementId 节点唯一标识
     * @param needUpdatedAttr 需要更新的节点属性
     */
    fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any>)

    /**
     * 获取当前节点的下一个节点
     * @param elementId 当前节点唯一标识
     * @return 下一个节点信息
     */
    fun nextNode(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 获取当前节点的前一个节点
     * @param elementId 当前节点唯一标识
     * @return 前一个节点信息
     */
    fun preNode(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 根据elementId获取节点的详细信息
     * @param elementId 节点Id
     * @return 节点的详细信息
     */
    fun selectByElementId(elementId: String?): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 清除所有数据
     */
    fun clearAllData()
}