package top.emilejones.hhu.domain.knowledge

class KnowledgeDocument(
    val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
    val knowledgeCatalogId: String
) {
}