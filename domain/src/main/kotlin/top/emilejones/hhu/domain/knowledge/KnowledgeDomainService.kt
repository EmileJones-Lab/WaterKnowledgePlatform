package top.emilejones.hhu.domain.knowledge


import top.emilejones.hhu.domain.knowledge.event.KnowledgeDocumentAddedToCatalogEvent
import java.time.Instant

/**
 * 知识库相关领域服务，承担跨聚合的业务编排与校验。
 * @author EmileJones
 */
class KnowledgeDomainService {
    /**
     * 添加知识文件到知识库中，如果违规操作会抛出异常。
     * 负责校验业务规则并生成领域事件，具体的持久化与事件发布由上层应用服务处理。
     * @param knowledgeDocument 知识文件
     * @param knowledgeCatalog 知识库
     * @return KnowledgeDocumentAddedToCatalogEvent 绑定成功的领域事件
     */
    fun bindKnowledgeDocumentToKnowledgeCatalog(
        knowledgeDocument: KnowledgeDocument,
        knowledgeCatalog: KnowledgeCatalog
    ): KnowledgeDocumentAddedToCatalogEvent {
        if (knowledgeCatalog.type == KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) {
            require(knowledgeDocument.type == KnowledgeDocumentType.STRUCTURE_SPLITTER) { "结构化知识库只能存储结构化内容" }
        }
        
        return KnowledgeDocumentAddedToCatalogEvent(
            knowledgeDocument = knowledgeDocument,
            knowledgeCatalog = knowledgeCatalog,
            bindTime = Instant.now()
        )
    }
}
