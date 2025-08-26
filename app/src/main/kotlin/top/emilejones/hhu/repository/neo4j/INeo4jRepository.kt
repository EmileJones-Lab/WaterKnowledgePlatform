package top.emilejones.hhu.repository.neo4j

import top.emilejones.hhu.domain.TextNode
import top.emilejones.hhu.repository.neo4j.po.Neo4jFileNode
import top.emilejones.hhu.repository.neo4j.po.Neo4jTextNode
import top.emilejones.hhu.repository.neo4j.po.Neo4jRelationship

interface INeo4jRepository: AutoCloseable {
    fun insertNeo4jTextNode(node: Neo4jTextNode): Neo4jTextNode
    fun insertNeo4jFileNode(node: Neo4jFileNode): Neo4jFileNode
    fun insertNeo4jRelationship(relationship: Neo4jRelationship): Neo4jRelationship
    fun insertTree(rootNode: TextNode)
    fun searchNeo4jTextNodeByFilename(filename: String): MutableList<Neo4jTextNode>
}