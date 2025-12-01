package top.emilejones.hhu.domain.document.infrastruction

import top.emilejones.hhu.domain.document.SourceDocument
import java.io.InputStream
import java.util.Optional

interface SourceDocumentRepository {
    fun openContent(path: String): InputStream
    fun findSourceDocumentById(sourceDocumentId: String): Optional<SourceDocument>
}