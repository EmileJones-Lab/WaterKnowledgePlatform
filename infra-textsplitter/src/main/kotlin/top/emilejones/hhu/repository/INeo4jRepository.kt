package top.emilejones.hhu.repository

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode

/**
 * @author EmileJones
 */
interface INeo4jRepository {
    /**
     * 按照树状结构插入
     * @param rootNode 整个树状结构的根节点，这个根节点是空的头节点，它不会被插入。
     */
    fun insertTree(rootNode: TextNodeDTO)

    // 基本插入操作
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode
    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode
    fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship


    // 基本查询功能
    fun searchNeo4jFileNodeByNodeId(id: String): Neo4jFileNode?
    fun searchNeo4jFileNodeByFileId(fileId: String): Neo4jFileNode?
    fun searchNeo4jTextNodeByFileId(fileId: String): MutableList<Neo4jTextNode>
    fun searchNeo4jTextNodeByNodeId(id: String): Neo4jTextNode?
    fun searchNeo4jFileNodeByTextNode(id: String): Neo4jFileNode

    /**
     * 根据elementId去修改节点的属性，如果属性不存在，则添加。
     * @param elementId 节点唯一标识
     * @param needUpdatedAttr 需要更新的节点属性
     */
    fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any?>)

    /**
     * 获取当前节点的下一个节点
     * @param id 当前节点唯一标识
     * @return 下一个节点信息
     */
    fun nextNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 获取当前节点的前一个节点
     * @param id 当前节点唯一标识
     * @return 前一个节点信息
     */
    fun preNode(id: String): Pair<Neo4jFileNode, Neo4jTextNode>?

    /**
     * 获取当前节点的父亲节点的信息
     *
     * @param id 当前节点唯一标识
     * @return 父亲节点信息
     */
    fun parent(id: String): Neo4jTextNode?

    /**
     * 获取当前节点的兄弟节点，并且按照顺序排序
     *
     * @param id 当前节点唯一标识
     * @return 兄弟节点信息
     */
    fun siblings(id: String): List<Neo4jTextNode>

    /**
     * 获取当前节点的孩子节点
     *
     * @param id 当前节点唯一标识
     * @return 孩子节点信息
     */
    fun children(id: String): List<Neo4jTextNode>

    /**
     * 清除所有数据
     */
    fun clearAllData()
}