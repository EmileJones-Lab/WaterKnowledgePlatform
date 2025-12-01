package top.emilejones.hhu.domain.document.infrastruction

import top.emilejones.hhu.domain.document.SourceDocument
import java.io.InputStream
import java.util.Optional

/**
 * 源文件仓储接口，负责文件与元数据的访问。
 * @author EmileJones
 */
interface SourceDocumentRepository {
    /**
     * 打开源文件内容流。
     */
    fun openContent(path: String): InputStream

    /**
     * 根据标识查找源文件信息。
     */
    fun findSourceDocumentById(sourceDocumentId: String): Optional<SourceDocument>
}
