package top.emilejones.hhu.knowledge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.knowledge.pojo.dto.CollectionDocumentDto;
import top.emilejones.hhu.knowledge.pojo.po.CollectionDocumentPo;

import java.util.List;

@Mapper
public interface CollectionDocumentMapper {

    /**
     * 将向量化文件与知识库绑定
     * @param collectionDocumentPo
     */
    void bind(CollectionDocumentPo collectionDocumentPo);

    /**
     * 判断当前绑定记录是否存在
     * @param documentId
     * @param catalogId
     * @return int
     */
    int selectFromCollectionDocument(@Param("documentId") String documentId, @Param("catalogId") String catalogId);

    /**
     * 批量删除指定知识库中绑定的向量化文件
     * @param knowledgeCatalogId
     * @param knowledgeDocumentIdList
     */
    void deleteKnowledgeDocumentFromKnowledgeCatalog(@Param("knowledgeCatalogId") String knowledgeCatalogId, @Param("knowledgeDocumentIdList") List<String> knowledgeDocumentIdList);

    List<CollectionDocumentDto> selectByCatalogId(@Param("knowledgeCatalogId") String knowledgeCatalogId);

    /**
     * 硬删除（物理删除）
     * @param id
     */
    void hardDelete(String id);
}
