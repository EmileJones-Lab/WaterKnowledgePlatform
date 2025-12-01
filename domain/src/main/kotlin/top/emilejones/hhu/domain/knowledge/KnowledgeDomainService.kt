package top.emilejones.hhu.domain.knowledge


/**
 * 知识库相关领域服务，承担跨聚合的业务编排与校验（不持久化、不发布事件）。
 * @author EmileJones
 */
class KnowledgeDomainService {
    /**
     * 添加知识文件到知识库中，如果违规操作会抛出异常。
     * 仅负责校验和业务规则检查，具体的持久化与事件发布由上层应用服务处理。
     * @param knowledgeDocument 知识文件
     * @param knowledgeCatalog 知识库
     */
    fun bindKnowledgeDocumentToKnowledgeCatalog(
        knowledgeDocument: KnowledgeDocument,
        knowledgeCatalog: KnowledgeCatalog
    ) {
        if (knowledgeCatalog.type == KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) {
            require(knowledgeDocument.type == KnowledgeDocumentType.STRUCTURE_SPLITTER) { "结构化知识库只能存储结构化内容" }
        }
    }
}
