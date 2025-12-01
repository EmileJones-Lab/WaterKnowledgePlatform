package top.emilejones.hhu.domain.pipeline.infrastructure.repository

import top.emilejones.hhu.domain.pipeline.MarkdownDocument
import java.io.InputStream

interface MarkdownDocumentRepository {
    fun save(markdownDocument: MarkdownDocument, content: InputStream)
    fun findById(id: String): MarkdownDocument
    fun openContent(filePath: String): InputStream
}