package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

interface KnowledgeDocumentRepository {
    fun findByKnowledgeCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int): List<KnowledgeDocument>
    fun findCandidateKnowledgeDocumentKnowledgeCatalogId(knowledgeCatalogId: String): List<KnowledgeDocument>
    fun save(knowledgeDocument: KnowledgeDocument)
    fun delete(knowledgeDocumentId: String)
}