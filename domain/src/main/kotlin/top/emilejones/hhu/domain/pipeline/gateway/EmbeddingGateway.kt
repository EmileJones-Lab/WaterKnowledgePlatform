package top.emilejones.hhu.domain.pipeline.gateway

import top.emilejones.hhu.common.Result

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
    @Deprecated("建议使用 embed(fileNodeId: String) 以支持完整的领域逻辑封装", ReplaceWith("embed(fileNodeId)"))
    fun embed(textList: List<String>): List<List<Float>>

    /**
     * 根据文件节点 ID 对其下所有文本节点以及文件节点本身进行摘要向量化并保存。
     *
     * 约定：
     * - 该方法会查询 [fileNodeId] 对应的 FileNode 及其下属的所有 TextNode。
     * - 该方法需要 [fileNodeId] 对应的 FileNode 执行了 summary 方法生成了摘要，否则返回错误。
     * - 对 FileNode 和 TextNode 的 摘要 进行向量化，并将向量结果回填至 FileNode 或 TextNode 中。
     * - 更新 FileNode 和 TextNode 状态为已向量化。
     * - 最终将所有更新后的节点持久化到存储中（如 Neo4j）。
     *
     * @param fileNodeId 成功提取结构后的 FileNode 唯一 ID
     * @return 封装了 fileNodeId 的 Result，成功时包含 fileNodeId，失败时包含异常信息
     */
    fun embed(fileNodeId: String): Result<String>
}
