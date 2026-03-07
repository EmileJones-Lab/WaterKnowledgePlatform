package top.emilejones.hhu.textsplitter.repository.neo4j

import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jRelationship
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode

/**
 * Neo4j 命令仓库接口
 * 负责数据的增删改操作
 *
 * @author EmileJones
 */
interface INeo4jCommandRepository {

    /**
     * 插入Neo4j文本节点
     *
     * @param node 文本节点对象
     * @return 插入后的文本节点
     */
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode

    /**
     * 插入Neo4j文件节点
     *
     * @param node 文件节点对象
     * @return 插入后的文件节点
     */
    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode

    /**
     * 插入Neo4j关系
     *
     * @param relationship 关系对象
     * @return 插入后的关系
     */
    fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship

    /**
     * 根据elementId去修改节点的属性，如果属性不存在，则添加。
     *
     * @param elementId 节点唯一标识
     * @param needUpdatedAttr 需要更新的节点属性
     */
    fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any?>)

    /**
     * 通过节点id软删除TextNode
     *
     * @param id 节点ID
     */
    fun deleteTextNodeById(id: String)

    /**
     * 通过节点id硬删除TextNode
     *
     * @param id 节点ID
     */
    fun hardDeleteTextNodeById(id: String)

    /**
     * 通过文件节点id软删除FileNode以及其关联关系
     *
     * @param id 文件节点ID
     */
    fun deleteFileNodeById(id: String)

    /**
     * 通过文件节点id硬删除FileNode以及其关联关系
     *
     * @param id 文件节点ID
     */
    fun hardDeleteFileNodeById(id: String)

    /**
     * 清除所有数据
     */
    fun clearAllData()
}
