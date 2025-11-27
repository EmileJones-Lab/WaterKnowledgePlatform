package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository

class KnowledgeDocument(
    val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
    var knowledgeCatalogId: String?
) {

    fun addToKnowledgeCatalog(knowledgeCatalogId: String, knowledgeDocumentRepository: KnowledgeDocumentRepository) {
        require(knowledgeCatalogId.isNotBlank()) { "知识库唯一Id不能为空" }
        this.knowledgeCatalogId = knowledgeCatalogId
        knowledgeDocumentRepository.save(this)
    }
}