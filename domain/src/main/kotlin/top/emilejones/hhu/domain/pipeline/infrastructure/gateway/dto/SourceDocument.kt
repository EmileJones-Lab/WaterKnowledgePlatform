package top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto

import java.io.InputStream

class SourceDocument(
    val id: String,
    val name: String,
    val inputStream: InputStream
) {
}