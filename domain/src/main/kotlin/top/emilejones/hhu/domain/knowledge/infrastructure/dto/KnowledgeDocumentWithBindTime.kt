package top.emilejones.hhu.domain.knowledge.infrastructure.dto

import top.emilejones.hhu.domain.knowledge.KnowledgeDocument
import java.time.Instant

/**
 * 带有绑定信息的知识文档对象，用于列表展示等场景。
 * 包含了文档本体以及该文档与特定目录绑定的时间上下文。
 * @author EmileJones
 */
data class KnowledgeDocumentWithBindTime(
    val knowledgeDocument: KnowledgeDocument,
    val bindTime: Instant
)
