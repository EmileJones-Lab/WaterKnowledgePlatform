package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.FileNode
import top.emilejones.hhu.domain.pipeline.TextNode
import java.util.*

/**
 * 文件与文本节点仓储接口，负责节点的读取、写入与召回。
 * @author EmileJones
 */
interface NodeRepository {

    /**
     * 根据文件节点Id查询文件节点。
     *
     * 约定：未命中时返回 `Optional.empty()`。
     *
     * @param fileNodeId FileNode唯一Id
     * @return 根据Id查找到的FileNode，未找到时返回 Optional.empty
     */
    fun findFileNodeByFileNodeId(fileNodeId: String): Optional<FileNode>

    /**
     * 查询文件节点下的文本节点列表。
     *
     * 约定：返回顺序由实现定义（通常按层级与顺序号）。
     *
     * @param fileNodeId FileNode唯一Id
     * @return 此FileNode下的所有TextNode，顺序由实现定义（通常按层级与顺序号）
     */
    fun findTextNodeListByFileNodeId(fileNodeId: String): List<TextNode>

    /**
     * 根据标识查询单个文本节点。
     *
     * 约定：未命中时返回 `Optional.empty()`。
     *
     * @param textNodeId TextNode唯一Id
     * @return 根据Id查找到的TextNode，未找到时返回 Optional.empty
     */
    fun findTextNodeByTextNodeId(textNodeId: String): Optional<TextNode>

    /**
     * 保存TextNode相关信息，存在则更新，不存在则创建。
     *
     * 约定：采用 upsert 语义。
     *
     * @param textNode 需要添加或者修改的TextNode
     */
    fun saveTextNode(textNode: TextNode)

    /**
     * 根据问题召回相关节点。
     *
     * 约定：依赖向量库/全文检索结果，需保证可重复调用且按照相关度排序（排序规则由实现决定）。
     *
     * @param query 用户问题
     * @param collectionName 目标知识库/向量集合
     * @return 和问题相关的节点列表
     */
    fun recallTextNode(query: String, collectionName: String): List<TextNode>

    /**
     * 删除指定的文件节点以及其下的所有子节点。
     *
     * 约定：调用方需保证Id合法；实现应执行级联删除，确保FileNode及其所有关联的TextNode等子节点被清理。
     *
     * @param id FileNode唯一Id
     */
    fun deleteAllNodeByFileNodeId(id: String)
}
