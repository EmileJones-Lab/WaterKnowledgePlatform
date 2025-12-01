package top.emilejones.hhu.domain.knowledge

import top.emilejones.hhu.domain.AggregateRoot

class KnowledgeDocument(
    override val id: String,
    val name: String,
    val embeddingMissionId: String,
    val type: KnowledgeDocumentType,
) : AggregateRoot<String>(id)