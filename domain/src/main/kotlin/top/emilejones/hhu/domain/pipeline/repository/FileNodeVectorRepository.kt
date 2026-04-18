package top.emilejones.hhu.domain.pipeline.repository

import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.result.FileNode

/**
 * 文件节点向量库仓库，负责文件级别向量的存储、删除与检索。
 */
interface FileNodeVectorRepository {
    /**
     * 将指定的文件节点向量信息存入向量数据库。
     *
     * 约定：
     * - 调用方需保证传入的所有 FileNode 均已包含向量信息。
     * - 如果列表中存在任何一个节点缺失向量信息，则整个保存操作失败。
     * - 采用批量保存模式。
     *
     * @param fileNodes 需要存入的文件节点列表
     * @param collectionName 目标向量库/集合名称
     * @return 操作结果包装
     */
    fun saveFileNodeToVectorDatabase(fileNodes: List<FileNode>, collectionName: String): Result<Void>

    /**
     * 创建一个新的文件向量集合。
     *
     * @param collectionName 集合名称
     * @return 操作结果包装
     */
    fun createCollection(collectionName: String): Result<Void>

    /**
     * 从向量数据库中删除指定的文件节点向量。
     *
     * @param fileNodeIds 待删除的文件节点 ID 列表
     * @param collectionName 目标向量库/集合名称
     * @return 操作结果包装
     */
    fun deleteFileNodeFromVectorDatabases(fileNodeIds: List<String>, collectionName: String): Result<Void>

    /**
     * 根据问题召回相关的文件节点。
     *
     * @param query 用户问题
     * @param collectionName 目标知识库/向量集合
     * @param fileNodeIdList 可选：指定召回的文件范围。若为空则全库检索。
     * @return 和问题相关的文件节点列表
     */
    fun recallFileNode(query: String, collectionName: String, fileNodeIdList: List<String>? = null): List<FileNode>
}
