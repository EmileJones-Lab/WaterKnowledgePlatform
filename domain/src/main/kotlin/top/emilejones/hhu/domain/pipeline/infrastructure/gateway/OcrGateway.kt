package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.ProcessedDocument
import java.io.InputStream

interface OcrGateway {
    fun startOcr(input: InputStream): ProcessedDocument
}