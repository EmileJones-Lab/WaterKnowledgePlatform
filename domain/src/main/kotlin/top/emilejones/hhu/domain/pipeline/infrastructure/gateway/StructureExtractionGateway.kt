package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.FileNode
import java.io.InputStream

/**
 * 文档结构抽取网关，封装外部切分能力。
 * @author EmileJones
 */
interface StructureExtractionGateway {
    /**
     * 执行结构抽取，返回文件节点。
     */
    fun extract(inputStream: InputStream): FileNode
}
