package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import java.io.InputStream

interface OcrGateway {
    fun minerU(input: InputStream): InputStream
}