package top.emilejones.hhu.repository.neo4j

import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jRelationship
import top.emilejones.hhu.domain.po.Neo4jTextNode

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
    fun searchFileNodeByFileName(filename: String): Neo4jFileNode?
    fun updateNodeByElementId(elementId: String, needUpdatedAttr: Map<String, Any>)
}