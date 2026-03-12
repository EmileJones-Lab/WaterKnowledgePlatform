package top.emilejones.hhu.domain.document.repository

import top.emilejones.hhu.domain.document.SourceDocument
import java.io.InputStream
import java.util.Optional

/**
 * 源文件仓储接口（DDD 领域层）
 *
 * 核心职责：定义源文件相关的数据访问契约，屏蔽底层存储细节（如 MySQL、MinIO 等）。
 * 具体实现位于基础设施层（infra-document），遵循 DDD 依赖倒置原则。
 *
 * @author EmileJones
 * @author Yeyezhi
 */
interface SourceDocumentRepository {
    /**
     * 打开源文件内容流
     *
     * 根据提供的存储路径（通常从 [findSourceDocumentById] 获取），从底层存储（如 MinIO）读取文件内容。
     *
     * @param path 文件在存储系统中的相对路径
     * @return [InputStream] 源文件的字节输入流，支持 PDF、DOC、TXT 等格式
     */
    fun openContent(path: String): InputStream

    /**
     * 根据唯一标识查询文件元数据
     *
     * 从数据库（如 MySQL col_file 表）查询对应记录，并映射为领域层的 [SourceDocument] 聚合根。
     * 元数据包含文件名、目录 ID、存储路径、文件类型等核心信息，不包含文件内容。
     * 注意：filePath 仅返回资源路径字符串，不包含域名、协议或端口。
     *
     * @param sourceDocumentId 源文件唯一标识
     * @return [Optional] 包装的源文件领域对象。若不存在则返回 [Optional.empty]
     */
    fun findSourceDocumentById(sourceDocumentId: String): Optional<SourceDocument>
}
