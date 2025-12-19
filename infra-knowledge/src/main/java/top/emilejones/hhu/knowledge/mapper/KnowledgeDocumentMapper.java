package top.emilejones.hhu.knowledge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;

import java.util.List;
import java.util.Set;

@Mapper
public interface KnowledgeDocumentMapper {

    /**
     * 根据id查询向量化文件
     * @param documentId
     * @return KnowledgeDocumentDto
     */
    KnowledgeDocumentDto find(@Param("documentId") String documentId, @Param("keyWord") String keyWord);

    /**
     * 新增向量化文件
     * @param knowledgeDocumentDto
     */
    void save(KnowledgeDocumentDto knowledgeDocumentDto);

    /**
     * 更新向量化文件
     * @param knowledgeDocumentDto
     */
    void update(KnowledgeDocumentDto knowledgeDocumentDto);

    /**
     * 分页查询指定知识库下的向量化文件
     * @param knowledgeCatalogId
     * @param limit
     * @param offset
     * @return List<KnowledgeDocumentDto> 可以为empty 不能为null
     */
    List<KnowledgeDocumentDto> findByKnowledgeCatalogId(@Param("knowledgeCatalogId") String knowledgeCatalogId, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 查询知识库类型
     * @param knowledgeCatalogId
     * @return KnowledgeCatalogType
     */
    KnowledgeCatalogType findKnowledgeCatalogType(@Param("knowledgeCatalogId") String knowledgeCatalogId);

    /**
     * 查询指定知识库的候选向量化文件
     * @param knowledgeCatalogId
     * @param types
     * @return List<KnowledgeDocumentDto> 可以为empty 不能为null
     */
    List<KnowledgeDocumentDto> findCandidateDocument(@Param("knowledgeCatalogId") String knowledgeCatalogId, @Param("types") List<KnowledgeDocumentType> types, @Param("keyWord") String keyWord);

    /**
     * 查询当前所有绑定该向量化文件的知识库信息
     * @param knowledgeDocumentId
     * @return
     */
    List<KnowledgeCatalogDto> findKnowledgeCatalogByKnowledgeDocumentId(@Param("knowledgeDocumentId") String knowledgeDocumentId);

    /**
     * 根据id查询对应的向量化文件
     * @param knowledgeDocumentId
     * @return KnowledgeDocument
     */
    KnowledgeDocumentDto findKnowledgeDocumentByKnowledgeDocumentId(@Param("knowledgeDocumentId") String knowledgeDocumentId);
}
