package top.emilejones.hhu.domain.pipeline.gateway

import top.emilejones.hhu.common.Result

/**
 * 文档结构抽取网关，对外暴露切分与持久化能力。
 * @author EmileJones
 */
interface StructureExtractionGateway {
    /**
     * 解析并抽取文档结构，将 Markdown 文本转换为树形节点，然后存入数据库中。
     *
     * 约定：
     * - 入参必须是已经过校验、符合 Markdown 规范的文本二进制内容。
     * - 返回的根节点为虚拟 NULL 节点，仅作为树形结构的挂载点，其中的 FileNode 不自动绑定 fileId，需由调用方补全元数据。
     * - 节点 ID 由实现内部生成（UUID），调用方无需补充；顺序、层级、前后驱关系需在实现内保证正确。
     * - 封装在 Result 中，成功时返回 FileNodeId，失败时包含详细的错误原因。
     *
     * @param input 预处理后的 Markdown 二进制内容
     * @param sourceDocumentId 源文件ID，用来绑定生成的数据与源文件之间的关系
     * @return 封装了 FileNodeId 的 Result
     */
    fun extract(input: ByteArray, sourceDocumentId: String): Result<String>

    /**
     * 针对已持久化的文档结构，递归生成各级节点的语义摘要并回填。
     *
     * 约定：
     * - 调用前提：必须保证 [sourceDocumentId] 对应的文档结构已通过 extract 方法成功持久化。
     * - 摘要策略：采用自底向上的递归生成方式。叶子节点对原文进行摘要，中间节点对子节点摘要列表进行聚合。
     * - 数据存储：生成的摘要将直接更新至数据库中各 TextNode 的 summary 字段，根节点摘要将同步至 FileNode 的 fileAbstract。
     * - 封装在 Result 中，成功时返回该文档树的根节点 ID (FileNodeId)，失败时包含详细异常信息。
     *
     * @param sourceDocumentId 源文件ID，用于检索对应的文档树结构
     * @return 封装了 FileNodeId 的 Result
     */
    fun summary(sourceDocumentId: String): Result<String>
}
