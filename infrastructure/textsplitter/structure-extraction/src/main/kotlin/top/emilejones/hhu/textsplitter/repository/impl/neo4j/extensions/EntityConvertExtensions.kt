package top.emilejones.hhu.textsplitter.repository.impl.neo4j.extensions

import top.emilejones.hhu.domain.result.FileNode
import top.emilejones.hhu.domain.result.TextNode
import top.emilejones.hhu.textsplitter.domain.dto.FileNodeDTO
import top.emilejones.hhu.textsplitter.domain.dto.TextNodeDTO
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jFileNode
import top.emilejones.hhu.textsplitter.domain.po.neo4j.Neo4jTextNode

/**
 * 将 TextNodeDTO 转换为 Neo4jTextNode PO 对象。
 * 执行逻辑：映射基础属性并将 `isDelete` 默认置为 false。
 */
fun TextNodeDTO.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = this.vector,
        isDelete = false,
        summary = this.summary
    )
}

/**
 * 将 FileNodeDTO 转换为 Neo4jFileNode PO 对象。
 * 执行逻辑：检查 `fileId` 是否为空，并映射相关属性。
 */
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

/**
 * 将 Neo4jTextNode PO 转换为 TextNodeDTO。
 * 执行逻辑：执行字段的一一映射转换。
 */
fun Neo4jTextNode.toTextNodeDTO(): TextNodeDTO {
    return TextNodeDTO(
        id = this.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        summary = this.summary,
        vector = this.vector
    )
}

/**
 * 将 TextNode 结果对象转换为 Neo4jTextNode PO。
 * 执行逻辑：映射字段并初始化 `isDelete` 为 false。
 */
fun TextNode.toNeo4jTextNode(): Neo4jTextNode {
    return Neo4jTextNode(
        id = this.id,
        text = this.text,
        seq = this.seq,
        level = this.level,
        type = this.type,
        vector = this.vector,
        isDelete = false,
        summary = this.summary
    )
}

/**
 * 将 FileNode 结果对象转换为 Neo4jFileNode PO。
 * 执行逻辑：映射字段。
 */
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

/**
 * 将 Neo4jFileNode PO 转换为 FileNode 结果对象。
 * 执行逻辑：执行字段映射。
 */
fun Neo4jFileNode.asFileNode(): FileNode {
    return FileNode(
        id = this.id,
        sourceDocumentId = this.fileId,
        isEmbedded = this.isEmbedded,
        fileAbstract = this.fileAbstract,
        vector = this.vector
    )
}

/**
 * 将 Neo4jTextNode PO 转换为 TextNode 结果对象。
 * 执行逻辑：根据向量是否存在判断 `isEmbedded` 状态。
 */
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

/**
 * 将 Neo4jTextNode PO 转换为关联了具体 FileNode 的 TextNode 结果对象。
 * 执行逻辑：映射字段并关联外部 FileNode ID。
 */
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

/**
 * 将 Neo4jFileNode PO 转换为 FileNodeDTO。
 * 执行逻辑：映射基础属性。
 */
fun Neo4jFileNode.toFileNodeDTO(): FileNodeDTO {
    return FileNodeDTO(
        id = this.id,
        fileId = this.fileId,
        fileAbstract = this.fileAbstract
    )
}
