package top.emilejones.hhu.domain.pipeline.event

import top.emilejones.hhu.domain.DomainEvent
import java.time.Instant

/**
 * 向量入库失败事件
 * 当 Pipeline 无法将 Document 存入指定的 KnowledgeCatalog (Milvus Collection) 时触发
 * @param embeddingMissionId 失败的向量化任务ID
 * @param milvusCollectionName 尝试入库的Milvus Collection名称
 * @param reason 失败原因
 */
data class VectorIndexFailedEvent(
    val embeddingMissionId: String,
    val milvusCollectionName: String,
    val reason: String
) : DomainEvent()