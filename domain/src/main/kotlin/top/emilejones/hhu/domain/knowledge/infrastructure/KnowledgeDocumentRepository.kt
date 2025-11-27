package top.emilejones.hhu.domain.knowledge.infrastructure

import top.emilejones.hhu.domain.knowledge.KnowledgeDocument

interface KnowledgeDocumentRepository {
    fun selectByKnowledgeCatalogId(knowledgeCatalogId: String, limit: Int, offset: Int): List<KnowledgeDocument>
    fun selectCandidateKnowledgeDocumentKnowledgeCatalogId(): List<KnowledgeDocument>
    fun save(knowledgeDocument: KnowledgeDocument)
    fun delete(knowledgeDocumentId: String)
}