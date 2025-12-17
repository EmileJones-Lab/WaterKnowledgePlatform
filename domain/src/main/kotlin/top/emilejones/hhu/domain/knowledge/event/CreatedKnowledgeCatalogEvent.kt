package top.emilejones.hhu.domain.knowledge.event

import top.emilejones.hhu.domain.DomainEvent
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog

data class CreatedKnowledgeCatalogEvent(
    val newKnowledgeCatalog: KnowledgeCatalog
) : DomainEvent() {}