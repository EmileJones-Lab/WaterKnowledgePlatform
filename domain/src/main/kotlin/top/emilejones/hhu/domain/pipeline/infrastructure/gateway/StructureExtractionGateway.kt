package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO
import java.io.InputStream

/**
 * 文档结构抽取网关，封装外部切分能力。
 * @author EmileJones
 */
interface StructureExtractionGateway {
    /**
     * 执行结构抽取，返回文件节点。
     * @param inputStream 处理好的格式正确的markdown文件
     * @return 结构提取完毕后的结构，返回的是一个NULL类型的节点，通过它可便利所有其他节点
     */
    fun extract(inputStream: InputStream): TextNodeDTO

    /**
     * 保存树状结构到图数据库中
     * @param textNodeDTO NULL类型的节点，用来统一算法操作
     */
    fun save(textNodeDTO: TextNodeDTO)
}
