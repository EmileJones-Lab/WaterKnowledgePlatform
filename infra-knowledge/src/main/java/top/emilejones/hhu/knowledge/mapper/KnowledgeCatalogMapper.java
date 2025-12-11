package top.emilejones.hhu.knowledge.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.po.CollectionDocumentPo;

import java.util.List;

/**
 * KnowledgeCatalog持久层，完成和数据库的交互
 * @author EmileNathon
 */
@Mapper
public interface KnowledgeCatalogMapper {
    /**
     * 查询所有知识库信息
     * @return List<KnowledgeCatalogDto>
     */
    List<KnowledgeCatalogDto> findAll();

    /**
     * 根据id查询知识库信息
     * @param knowledgeCatalogId
     * @return KnowledgeCatalogDto
     */
    KnowledgeCatalogDto find(String knowledgeCatalogId);

    /**
     * 新增一个知识库
     * @param knowledgeCatalogDto
     */
    void save(KnowledgeCatalogDto knowledgeCatalogDto);

    /**
     * 更新存在的知识库
     * @param knowledgeCatalogDto
     */
    void update(KnowledgeCatalogDto knowledgeCatalogDto);
}

