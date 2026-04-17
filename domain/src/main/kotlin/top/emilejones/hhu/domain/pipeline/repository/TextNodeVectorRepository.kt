package top.emilejones.hhu.domain.pipeline.repository

import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.result.TextNode

/**
 * 向量库仓库，封装对向量数据库（如 Milvus）的操作。
 */
interface TextNodeVectorRepository {
    /**
     * 将指定文件节点列表下的所有已向量化的文本节点存入向量数据库。
     *
     * 约定：
     * - 实现方需根据 fileNodeIds 查询关联的文本节点。
     * - 仅存储已包含向量信息的节点。
     * - collection 不存在时的处理由实现决定（自动创建或抛出异常），失败需抛出可定位的异常。
     *
     * @param fileNodeIds 文件节点唯一标识列表
     * @param collectionName 目标向量库/集合名称，若不存在由实现决定是否创建
     * @return 操作结果包装，成功时无返回值
     */
    fun saveTextNodeToVectorDatabase(fileNodeIds: List<String>, collectionName: String): Result<Void>

    /**
     * 创建一个新的向量集合。
     *
     * 该方法用于在向量数据库中创建一个具有指定名称的新 collection。
     * 如果 collection 已存在，会选择忽略此请求。
     *
     * @param collectionName 要创建的 collection 的名称。
     * @return 操作结果包装，成功时无返回值
     */
    fun createCollection(collectionName: String): Result<Void>

    /**
     * 从向量数据库中删除指定文件的所有文本节点。
     *
     * 约定：
     * - 由实现方负责确认集合存在与否，并处理不存在或删除失败的场景。
     *
     * @param fileNodeIds 待删除的文件节点 ID 列表
     * @param collectionName 目标向量库/集合名称
     * @return 操作结果包装，成功时无返回值
     */
    fun deleteTextNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void>

    /**
     * 根据问题召回相关节点。
     *
     * 约定：依赖向量库/全文检索结果，需保证可重复调用且按照相关度排序（排序规则由实现决定）。
     *
     * @param query 用户问题
     * @param collectionName 目标知识库/向量集合
     * @param fileNodeIdList 可选：指定召回的文件节点 ID 列表。如果为空或 null，则在全库进行召回。
     * @return 和问题相关的节点列表
     */
    fun recallTextNode(query: String, collectionName: String, fileNodeIdList: List<String>? = null): List<TextNode>
}
