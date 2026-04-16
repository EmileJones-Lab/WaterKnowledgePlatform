package top.emilejones.hhu.domain.pipeline.repository

import top.emilejones.hhu.domain.result.TextNode

/**
 * 向量库仓库，封装对向量数据库（如 Milvus）的操作。
 */
interface TextNodeVectorRepository {
    /**
     * 将向量化后的文本节点存入向量数据库。
     *
     * 约定：
     * - 调用方需保证传入节点已包含向量信息。
     * - collection 不存在时的处理由实现决定（自动创建或抛出异常），失败需抛出可定位的异常。
     *
     * @param textNodeList 已包含向量信息的文本节点
     * @param collectionName 目标向量库/集合名称，若不存在由实现决定是否创建
     */
    fun saveTextNodeToVectorDatabase(textNodeList: List<TextNode>, collectionName: String)

    /**
     * 创建一个新的向量集合。
     *
     * 该方法用于在向量数据库中创建一个具有指定名称的新 collection。
     * 如果 collection 已存在，会选择忽略此请求。
     *
     * @param collectionName 要创建的 collection 的名称。
     */
    fun createTextNodeCollection(collectionName: String)

    /**
     * 从向量数据库中删除指定文本节点。
     *
     * 约定：
     * - 由实现方负责确认集合存在与否，并处理不存在或部分删除失败的场景。
     * - textNodeIdList 应对应向量库内部存储的节点唯一标识。
     *
     * @param textNodeIdList 待删除的文本节点 ID 列表
     * @param collectionName 目标向量库/集合名称
     */
    fun deleteTextNodeFromVectorDatabases(textNodeIdList: List<String>, collectionName: String)

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
}
