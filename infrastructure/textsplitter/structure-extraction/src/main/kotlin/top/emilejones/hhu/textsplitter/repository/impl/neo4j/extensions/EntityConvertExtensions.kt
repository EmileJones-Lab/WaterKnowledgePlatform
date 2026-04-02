package top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions

import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.Neo4jTextNode

fun TextNodeDTO.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        summary = this.summary,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = null,
        isDelete = false
    )
}

fun FileNodeDTO.toNeo4jFileNode(): Neo4jFileNode {
    require(this.fileId.isNotBlank()) { "FileNodeDTO没有设置fileId" }
    return Neo4jFileNode(
        id = this.id,
        fileId = this.fileId,
        isEmbedded = false,
        isDelete = false,
        fileAbstract = this.fileAbstract
    )
}

fun TextNode.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        summary = this.summary,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = this.vector,
        isDelete = false
    )
}

fun FileNode.toNeo4jFileNode(): Neo4jFileNode {
    return Neo4jFileNode(
        id = this.id,
        fileId = this.sourceDocumentId,
        isEmbedded = this.isEmbedded,
        isDelete = false,
        fileAbstract = this.fileAbstract
    )
}

fun Neo4jFileNode.asFileNode(): FileNode {
    return FileNode(
        id = this.id,
        sourceDocumentId = this.fileId,
        isEmbedded = this.isEmbedded,
        fileAbstract = this.fileAbstract
    )
}

fun Neo4jTextNode.diff(other: Neo4jTextNode): Map<String, Any?> {
    val diff = mutableMapOf<String, Any?>()
    if (this.text != other.text) diff["text"] = other.text
    if (this.summary != other.summary) diff["summary"] = other.summary
    if (this.seq != other.seq) diff["seq"] = other.seq
    if (this.level != other.level) diff["level"] = other.level
    if (this.type != other.type) diff["type"] = other.type
    if (this.vector != other.vector) diff["vector"] = other.vector
    if (this.isDelete != other.isDelete) diff["isDelete"] = other.isDelete
    return diff
}

fun Neo4jFileNode.diff(other: Neo4jFileNode): Map<String, Any?> {
    val diff = mutableMapOf<String, Any?>()
    if (this.fileId != other.fileId) diff["fileId"] = other.fileId
    if (this.isEmbedded != other.isEmbedded) diff["isEmbedded"] = other.isEmbedded
    if (this.isDelete != other.isDelete) diff["isDelete"] = other.isDelete
    if (this.fileAbstract != other.fileAbstract) diff["fileAbstract"] = other.fileAbstract
    return diff
}

fun Neo4jTextNode.asTextNode(fileNode: Neo4jFileNode): TextNode {
    return TextNode(
        id = this.id,
        text = this.text,
        summary = this.summary,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = this.vector,
        fileNodeId = fileNode.id,
        isEmbedded = this.vector != null
    )
}
