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
     * 根据文件节点Id查询文件节点。
     * @param fileNodeId FileNode唯一Id
     * @return 根据Id查找到的FileNode
     */
    fun findFileNodeByFileNodeId(fileNodeId: String): Optional<FileNode>

    /**
     * 查询文件节点下的文本节点列表。
     * @param fileNodeId FileNode唯一Id
     * @return 此FileNode下的所有TextNode
     */
    fun findTextNodeListByFileNodeId(fileNodeId: String): List<TextNode>

    /**
     * 根据标识查询单个文本节点。
     * @param textNodeId TextNode唯一Id
     * @return 根据Id查找到的TextNode
     */
    fun findTextNodeByTextNodeId(textNodeId: String): Optional<TextNode>

    /**
     * 保存TextNode相关信息，如果节点已经存在，则修改相关属性
     * @param textNode 需要添加或者修改的TextNode
     */
    fun saveTextNode(textNode: TextNode)

    /**
     * 根据问题去召回相关的结果
     * @param query 用户问题
     * @param collectionName 需要去哪个知识库召回
     * @return 和问题相关的节点
     */
    fun recallTextNode(query: String, collectionName: String): List<TextNode>
}
