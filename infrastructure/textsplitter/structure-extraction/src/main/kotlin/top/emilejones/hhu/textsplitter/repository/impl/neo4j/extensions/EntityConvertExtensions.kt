package top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions

import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode

fun TextNodeDTO.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = this.vector,
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
        fileAbstract = this.fileAbstract,
        vector = null
    )
}

fun TextNode.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
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
        fileAbstract = this.fileAbstract,
        vector = this.vector
    )
}

fun Neo4jFileNode.asFileNode(): FileNode {
    return FileNode(
        id = this.id,
        sourceDocumentId = this.fileId,
        isEmbedded = this.isEmbedded,
        fileAbstract = this.fileAbstract,
        vector = this.vector
    )
}

fun Neo4jTextNode.asTextNode(fileNode: Neo4jFileNode? = null): TextNode {
    return TextNode(
        id = this.id,
        fileNodeId = fileNode?.id ?: "",
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        isEmbedded = this.vector != null,
        vector = this.vector,
        summary = this.summary
    )
}

fun Neo4jTextNode.asTextNode(fileNode: FileNode): TextNode {
    return TextNode(
        id = this.id,
        fileNodeId = fileNode.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        isEmbedded = this.vector != null,
        vector = this.vector,
        summary = this.summary
    )
}

fun Neo4jFileNode.toFileNodeDTO(): FileNodeDTO {
    return FileNodeDTO(
        id = this.id,
        fileId = this.fileId,
        fileAbstract = this.fileAbstract
    )
}
