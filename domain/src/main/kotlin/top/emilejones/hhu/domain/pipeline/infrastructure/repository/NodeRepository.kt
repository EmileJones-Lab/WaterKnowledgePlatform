package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.FileNode
import top.emilejones.hhu.domain.pipeline.TextNode
import java.util.*

interface NodeRepository {
    fun findFileNodeByFileNodeId(fileNodeElementId: String): Optional<FileNode>
    fun findTextNodeListByFileNodeId(fileNodeElementId: String): Optional<List<TextNode>>
    fun findTextNodeById(textNodeElementId: String): Optional<TextNode>
}