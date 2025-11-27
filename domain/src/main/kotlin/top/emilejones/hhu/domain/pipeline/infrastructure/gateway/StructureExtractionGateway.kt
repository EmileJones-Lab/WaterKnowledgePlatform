package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.FileNode

interface StructureExtractionGateway {
    fun extract(processedDocumentId: String): FileNode
}