package top.emilejones.hhu.adaptor

import top.emilejones.hhu.domain.pipeline.FileNode
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.OcrGateway
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.StructureExtractionGateway
import java.io.InputStream

class RagAdaptor : OcrGateway, StructureExtractionGateway, EmbeddingGateway {
    override fun minerU(input: InputStream): InputStream {
        TODO("Not yet implemented")
    }

    override fun extract(inputStream: InputStream): FileNode {
        TODO("Not yet implemented")
    }

    override fun embed(fileNodeId: String) {
        TODO("Not yet implemented")
    }
}