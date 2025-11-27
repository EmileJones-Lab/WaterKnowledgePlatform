package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

interface KnowledgeCatalogRepository {
    fun selectAll(): List<KnowledgeCatalog>
    fun select(knowledgeCatalogId: String): KnowledgeCatalog?
    fun save(knowledgeDocument: KnowledgeDocument)

}