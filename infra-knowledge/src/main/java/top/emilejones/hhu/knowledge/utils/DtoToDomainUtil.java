package top.emilejones.hhu.knowledge.utils;

import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;

public class DtoToDomainUtil {
    /**
     * 将knowledgeDocumentDto封装成KnowledgeDocument
     * @param knowledgeDocumentDto
     * @return KnowledgeDocument
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
     * 将knowledgeCatalogDto封装成KnowledgeCatalog
     * @param knowledgeCatalogDto
     * @return KnowledgeCatalog
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
