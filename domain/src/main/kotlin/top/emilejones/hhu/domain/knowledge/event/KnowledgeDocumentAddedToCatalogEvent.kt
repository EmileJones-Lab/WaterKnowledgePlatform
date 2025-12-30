package top.emilejones.hhu.domain.knowledge.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument
import java.time.Instant

/**
 * 添加知识文档到知识库目录的事件。
 * @author EmileJones
 */
data class KnowledgeDocumentAddedToCatalogEvent(
    val knowledgeDocument: KnowledgeDocument,
    val knowledgeCatalog: KnowledgeCatalog,
    val bindTime: Instant
) : DomainEvent()
