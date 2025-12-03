package top.emilejones.hhu.repository.impl.neo4j.extensions

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.FileNodeDTO
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import top.emilejones.hhu.domain.po.Neo4jFileNode
import top.emilejones.hhu.domain.po.Neo4jTextNode

fun TextNodeDTO.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = null
    )
}

fun FileNodeDTO.toNeo4jFileNode(): Neo4jFileNode {
    require(this.fileId != null) { "FileNodeDTO没有设置fileId" }
    return Neo4jFileNode(
        id = this.id,
        fileId = this.fileId!!,
        isEmbedded = false
    )
}