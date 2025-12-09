package top.emilejones.hhu.knowledge.service.Impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.utils.DtoToDomainUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentRepository {
    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    /**
     * 根据知识库id查询所有绑定的向量化文件
     * @param knowledgeCatalogId
     * @param limit
     * @param offset
     * @return List<KnowledgeDocument> 可以是empty，但不能为空
     */
    public @NotNull List<KnowledgeDocument> findByKnowledgeCatalogId(@NotNull String knowledgeCatalogId, int limit, int offset) {
        List<KnowledgeDocumentDto> knowledgeDocumentDtoList =  knowledgeDocumentMapper.findByKnowledgeCatalogId(knowledgeCatalogId, limit, offset);

        //将查询到的所有KnowledgeDocumentDto封装成KnowledgeDocument并返回
        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentDtoList.stream().map(DtoToDomainUtil::toDocumentDomain).toList();
        return knowledgeDocumentList;
    }

    /**
     *
     * @param knowledgeCatalogId
     * @return List<KnowledgeDocument> 可以为empty 不能为null
     */
    @NotNull
    public List<KnowledgeDocument> findCandidateKnowledgeDocumentKnowledgeCatalogId(@NotNull String knowledgeCatalogId) {
        // 根据catalogId去查询知识库类型
        KnowledgeCatalogType catalogType = knowledgeDocumentMapper.findKnowledgeCatalogType(knowledgeCatalogId);

        // 类型映射
        List<KnowledgeDocumentType> types = catalogTypeConvertToDocumentType(catalogType);

        // 查询当前知识库所有候选向量化文件
        List<KnowledgeDocumentDto> candidateDocumentList = knowledgeDocumentMapper.findCandidateDocument(knowledgeCatalogId, types);

        // 封装成KnowledgeDocument并返回
        List<KnowledgeDocument> knowledgeDocumentList = candidateDocumentList.stream().map(DtoToDomainUtil::toDocumentDomain).toList();

        return knowledgeDocumentList;
    }


    /**
     * 新增向量化文件，如果已存在就更新
     * @param knowledgeDocument
     */
    public void save(@NotNull KnowledgeDocument knowledgeDocument) {
        // 封装KnowledgeDocument对象到Dto中
        KnowledgeDocumentDto knowledgeDocumentDto = new KnowledgeDocumentDto();
        knowledgeDocumentDto.setDocumentId(knowledgeDocument.getId());
        knowledgeDocumentDto.setDocumentName(knowledgeDocument.getName());
        knowledgeDocumentDto.setEmbedId(knowledgeDocument.getEmbeddingMissionId());
        knowledgeDocumentDto.setType(knowledgeDocument.getType());
        knowledgeDocumentDto.setCreateTime(knowledgeDocument.getCreateTime());

        // 设置isDelete字段值，默认为1
        knowledgeDocumentDto.setIsDelete(DeleteConstant.EXIST);

        // 判断当前向量化文件是否已经存在数据库
        if (knowledgeDocumentMapper.find(knowledgeDocument.getId()) == null){
            // 不存在,就保存
            knowledgeDocumentMapper.save(knowledgeDocumentDto);
        }else {
            // 存在，就更新
            knowledgeDocumentMapper.update(knowledgeDocumentDto);
        }
    }

    /**
     * 根据documentId删除对应的向量化文件，这里使用的是软删除，就是更新isdelete字段
     * @param knowledgeDocumentId
     */
    public void delete(@NotNull String knowledgeDocumentId) {
        KnowledgeDocumentDto knowledgeDocumentDto = new KnowledgeDocumentDto();
        // 对需要删除的信息封装成KnowledgeDocumentDto对象
        knowledgeDocumentDto.setDocumentId(knowledgeDocumentId);
        knowledgeDocumentDto.setIsDelete(DeleteConstant.DELETE);

        //删除对应的向量化文件，因为这里是软删除所以调用的是uodate方法
        knowledgeDocumentMapper.update(knowledgeDocumentDto);
    }

    /**
     * 根据documentId查询所有绑定该向量化文件的知识库
     * @param knowledgeDocumentId
     * @return List<KnowledgeCatalog> 可以为empty 不能是null
     */
    @NotNull
    public List<KnowledgeCatalog> findKnowledgeCatalogByKnowledgeDocumentId(@NotNull String knowledgeDocumentId) {
        // 查询所有的KnowledgeCatalogDto
        List<KnowledgeCatalogDto> knowledgeCatalogDtoList = knowledgeDocumentMapper.findKnowledgeCatalogByKnowledgeDocumentId(knowledgeDocumentId);

        // 封装成KnowledgeCatalog对象返回
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeCatalogDtoList.stream().map(DtoToDomainUtil::toCatalogDomain).toList();
        return knowledgeCatalogList;
    }


    /**
     * 将catalogType映射为documentType
     * @param catalogType
     * @return List<KnowledgeDocumentType>
     */
    private List<KnowledgeDocumentType> catalogTypeConvertToDocumentType(KnowledgeCatalogType catalogType) {
        return (catalogType == KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) ?
                List.of(KnowledgeDocumentType.STRUCTURE_SPLITTER) :
                List.of(
                        KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200,
                        KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_400,
                        KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_600
                );
    }
}
