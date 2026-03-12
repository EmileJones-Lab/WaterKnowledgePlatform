package top.emilejones.hhu.domain.pipeline.infrastructure

import top.emilejones.hhu.domain.result.TextNode

/**
 * 向量化网关，封装对底层嵌入模型与向量库的调用。
 */
interface EmbeddingGateway {
    /**
     * 将文本批量向量化。
     *
     * 约定：
     * - 保证返回向量与输入文本一一对应且顺序一致。
     * - 维度、模型名称等细节由实现方决定，出现异常需能定位具体原因（长度超限、模型不可用等）。
     *
     * @param textList 待向量化的文本段，元素顺序即返回顺序
     * @return 向量化结果列表，每个元素为与输入文本对应的向量
     */
    fun embed(textList: List<String>): List<List<Float>>

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
     * 如果 collection 已存在，具体实现可能会选择忽略、抛出异常或进行其他处理。
     *
     * @param collectionName 要创建的 collection 的名称。
     */
    fun createCollection(collectionName: String)
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
}
