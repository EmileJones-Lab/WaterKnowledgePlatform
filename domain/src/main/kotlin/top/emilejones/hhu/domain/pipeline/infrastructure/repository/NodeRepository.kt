package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.FileNode
import top.emilejones.hhu.domain.pipeline.TextNode
import java.util.*

/**
 * 文件与文本节点仓储接口。
 * @author EmileJones
 */
interface NodeRepository {
    /**
     * 根据文件节点标识查询文件节点。
     */
    fun findFileNodeByFileNodeId(fileNodeElementId: String): Optional<FileNode>

    /**
     * 查询文件节点下的文本节点列表。
     */
    fun findTextNodeListByFileNodeId(fileNodeElementId: String): Optional<List<TextNode>>

    /**
     * 根据标识查询单个文本节点。
     */
    fun findTextNodeById(textNodeElementId: String): Optional<TextNode>
}
