package top.emilejones.hhu.domain.document.infrastruction

import top.emilejones.hhu.domain.document.SourceDocument
import java.io.InputStream
import java.util.Optional

/**
 * 源文件仓储接口（DDD领域层）
 * 核心职责：定义源文件相关的数据访问契约，屏蔽底层存储细节（MySQL+MinIO）
 * 具体实现位于基础设施层（infra-document），遵循DDD依赖倒置原则
 * @author EmileJones
 * @author Yeyezhi
 */
interface SourceDocumentRepository {
    /**
     * 打开源文件内容流。（依赖findSourceDocumentById查询的结果路径，从MinIO根据路径获取文件并读取）
     *  @return InputStream 源文件的字节输入流，可通过流读取文件内容（如PDF、DOC、TXT等支持的格式）
     */
    fun openContent(path: String): InputStream

    /**
     * 根据源文件唯一标识查询文件元数据。
     * 从MySQL col_file表查询对应记录，并映射为领域层的SourceDocument聚合根
     * 元数据包含文件名、所属目录ID、MinIO文件路径、文件类型等核心信息，不包含文件内容
     * @return Optional<SourceDocument> 包装后的源文件领域对象：
     *                                   1. 存在对应记录：Optional包含SourceDocument实例（字段与col_file表映射）
     *                                   2. 不存在对应记录：Optional.empty()（无异常抛出）
     */
    fun findSourceDocumentById(sourceDocumentId: String): Optional<SourceDocument>
}
