package top.emilejones.hhu.domain.pipeline.gateway

import java.io.InputStream

/**
 * 文档结构抽取网关，对外暴露切分与持久化能力。
 * @author EmileJones
 */
interface StructureExtractionGateway {
    /**
     * 解析并抽取文档结构，将 Markdown 文本转换为树形节点，然后存入数据库中。
     *
     * 约定：
     * - 入参必须是已经过校验、符合 Markdown 规范的文本流。
     * - 返回的根节点为虚拟 NULL 节点，仅作为树形结构的挂载点，其中的 FileNode 不自动绑定 fileId，需由调用方补全元数据。
     * - 节点 ID 由实现内部生成（UUID），调用方无需补充；顺序、层级、前后驱关系需在实现内保证正确。
     * - 调用失败应抛出可定位原因的异常（格式错误、解析失败等），便于上层告警或重试。
     *
     * @param inputStream 预处理后的 Markdown 输入流，调用方负责管理生命周期（打开/关闭）
     * @param sourceDocumentId 源文件ID，用来绑定生成的数据与源文件之间的关系
     * @return FileNodeId
     */
    fun extract(inputStream: InputStream, sourceDocumentId: String): String
}
