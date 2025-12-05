package top.emilejones.hhu.knowledge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jetbrains.annotations.NotNull;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;

@Mapper
public interface KnowledgeDocumentMapper {
    KnowledgeDocumentDto find(@NotNull String document_id);

    void save(KnowledgeDocumentDto knowledgeDocumentDto);

    void update(KnowledgeDocumentDto knowledgeDocumentDto);
}
