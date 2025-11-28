package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot

class KnowledgeDocument(
    override val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
    var knowledgeCatalogId: String?
) : AggregateRoot<String>(id) {

    fun addToKnowledgeCatalog(knowledgeCatalogId: String) {
        require(knowledgeCatalogId.isNotBlank()) { "知识库唯一Id不能为空" }
        this.knowledgeCatalogId = knowledgeCatalogId
    }
}