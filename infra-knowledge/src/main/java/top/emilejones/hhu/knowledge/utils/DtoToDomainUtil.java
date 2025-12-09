package top.emilejones.hhu.knowledge.utils;

import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;

/**
 * DtoToDomainUtil 是一个工具类，负责将数据传输对象（DTO）转换为领域模型对象。
 * 它提供了将各种DTO封装成对应的领域对象的方法。
 */
public class DtoToDomainUtil {
    /**
     * 将 KnowledgeDocumentDto 对象封装成 KnowledgeDocument 领域模型对象。
     * @param knowledgeDocumentDto 待转换的 KnowledgeDocumentDto 对象。
     * @return 封装后的 KnowledgeDocument 领域模型对象。
     */
    public static KnowledgeDocument toDocumentDomain(KnowledgeDocumentDto knowledgeDocumentDto) {
        return new KnowledgeDocument(
                knowledgeDocumentDto.getDocumentId(),
                knowledgeDocumentDto.getDocumentName(),
                knowledgeDocumentDto.getEmbedId(),
                knowledgeDocumentDto.getType(),
                knowledgeDocumentDto.getCreateTime()
        );
    }

    /**
     * 将 KnowledgeCatalogDto 对象封装成 KnowledgeCatalog 领域模型对象。
     * @param knowledgeCatalogDto 待转换的 KnowledgeCatalogDto 对象。
     * @return 封装后的 KnowledgeCatalog 领域模型对象。
     */
    public static KnowledgeCatalog toCatalogDomain(KnowledgeCatalogDto knowledgeCatalogDto){
        return new KnowledgeCatalog(
                knowledgeCatalogDto.getKbId(),
                knowledgeCatalogDto.getKbName(),
                knowledgeCatalogDto.getColName(),
                knowledgeCatalogDto.getType()
        );
    }
}
