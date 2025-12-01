package top.emilejones.hhu.domain.knowledge.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

/**
 * 添加知识文档到知识库目录的事件。
 * @author EmileJones
 */
data class KnowledgeDocumentAddedToCatalogEvent(
    val knowledgeDocument: KnowledgeDocument,
    val knowledgeCatalog: KnowledgeCatalog
) : DomainEvent()
