package top.emilejones.hhu.domain.pipeline.service

import top.emilejones.hhu.domain.pipeline.event.VectorIndexFailedEvent
import java.time.Instant

/**
 * 向量索引领域服务
 * 
 * 职责：
 * 1. 封装向量入库相关的纯领域逻辑
 * 2. 充当 Event Factory，负责生产符合业务规则的事件对象
 */
class VectorIndexDomainService {

    /**
     * 生成向量入库失败事件
     * 
     * 这是一个纯函数（Pure Function）：
     * - 输入：上下文信息和错误原因
     * - 输出：构建好的 Domain Event 对象
     * - 副作用：无（不发布，不存库）
     */
    fun createIndexFailedEvent(
        embeddingMissionId: String,
        milvusCollectionName: String,
        error: Throwable
    ): VectorIndexFailedEvent {
        // 在这里，Domain 层可以控制事件内容的格式化，或者添加额外的业务元数据
        // 比如：规范化错误消息，或者决定是否属于严重错误
        val safeReason = error.message ?: "Unknown Error"
        
        return VectorIndexFailedEvent(
            embeddingMissionId = embeddingMissionId,
            milvusCollectionName = milvusCollectionName,
            reason = safeReason
        )
    }
}
