package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.SourceDocument

interface SourceDocumentGateway {
    fun getSourceDocument(sourceDocumentId: String): SourceDocument?
}