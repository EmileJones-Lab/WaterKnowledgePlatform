package top.emilejones.hhu.repository.impl.neo4j.extensions

import top.emilejones.hhu.domain.dto.FileNode
import top.emilejones.hhu.domain.dto.TextNode
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode

fun TextNode.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type
    )
}

fun FileNode.toNeo4jFileNode(): Neo4jFileNode {
    return Neo4jFileNode(
        fileName = this.fileName,
        isEmbedded = false
    )
}