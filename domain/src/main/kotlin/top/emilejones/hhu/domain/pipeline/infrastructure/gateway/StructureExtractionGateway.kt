package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.FileNode
import java.io.InputStream

interface StructureExtractionGateway {
    fun extract(inputStream: InputStream): FileNode
}