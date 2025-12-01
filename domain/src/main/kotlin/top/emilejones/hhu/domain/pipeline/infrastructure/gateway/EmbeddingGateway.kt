package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

/**
 * 向量化网关，调用底层嵌入实现。
 * @author EmileJones
 */
interface EmbeddingGateway {
    /**
     * 对指定文件节点执行向量化。
     */
    fun embed(fileNodeId: String)
}
