package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.MarkdownDocument
import java.io.InputStream

/**
 * Markdown 文档的存储接口。
 * @author EmileJones
 */
interface MarkdownDocumentRepository {
    /**
     * 保存文档与其内容；若同标识的文档已存在，将覆盖之前的元数据与文件内容。
     */
    fun save(markdownDocument: MarkdownDocument, content: InputStream)

    /**
     * 根据标识查询文档。
     */
    fun findById(id: String): MarkdownDocument

    /**
     * 打开文档内容流。
     */
    fun openContent(filePath: String): InputStream
}
