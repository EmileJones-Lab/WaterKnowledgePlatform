package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.ProcessedDocument
import java.io.InputStream
import java.util.Optional

/**
 * Markdown 文档的存储接口，负责文档元数据与正文内容的持久化。
 *
 * 约定：通用约束与实现细节以各方法注释为准。
 * @author EmileJones
 */
interface ProcessedDocumentRepository {
    /**
     * 保存文档与其内容。
     *
     * 约定：
     * - 应实现幂等/覆盖语义，确保同一标识重复写入不会产生脏数据。
     * - 内容流的打开与关闭由调用方负责；实现只消费输入流并持久化，不应尝试重置或重复读取流。
     *
     * @param processedDocument 文档元数据（标识、标题等）
     * @param content Markdown 正文内容流；调用方负责在写入完成后关闭流
     */
    fun save(processedDocument: ProcessedDocument, content: InputStream)

    /**
     * 根据标识查询文档元数据。
     *
     * 约定：未找到记录时返回 `Optional.empty()`；调用方需处理未命中分支。
     *
     * @param id 文档标识
     * @return 对应的文档元数据；未命中返回 Optional.empty
     */
    fun findById(id: String): Optional<ProcessedDocument>

    /**
     * 打开文档内容流，用于上层读取内容。
     *
     * 约定：
     * - 返回的流需由调用方关闭；未找到内容时应抛出可定位的异常。
     *
     * @param filePath 文档内容的存储路径或键
     * @return 文档内容流，调用方负责关闭
     */
    fun openContent(filePath: String): InputStream

    /**
     * 删除处理后文档（软删除）。
     *
     * 约定：
     * - 仅标记元数据为删除状态，保留物理文件与记录。
     * - 若 ID 不存在，实现应静默处理或抛出特定异常（视业务需求定，此处建议静默）。
     *
     * @param markdownDocumentId 处理后文档的唯一标识
     */
    fun delete(markdownDocumentId: String)
}
