package top.emilejones.hhu.knowledge.service.Impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.knowledge.*;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.CollectionDocumentDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.utils.DtoToDomainUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * KnowledgeDocumentServiceImpl 是 KnowledgeDocumentRepository 接口的实现类，
 * 负责处理知识文档相关的业务逻辑操作。
 * 它通过与 KnowledgeDocumentMapper 交互来持久化和检索知识文档数据。
 */
@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentRepository {
    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Autowired
    private CollectionDocumentMapper collectionDocumentMapper;

    /**
     * 根据知识库ID分页查询所有绑定的向量化文件。
     * @param knowledgeCatalogId 知识库目录的ID。
     * @param limit 每页查询的数量限制。
     * @param offset 查询的起始偏移量。
     * @return List<KnowledgeDocument> 绑定的向量化文件列表，可能为空但不会为null。
     */
    @Override
    public @NotNull List<KnowledgeDocument> findByKnowledgeCatalogId(@NotNull String knowledgeCatalogId, int limit, int offset) {
        List<KnowledgeDocumentDto> knowledgeDocumentDtoList = knowledgeDocumentMapper.findByKnowledgeCatalogId(knowledgeCatalogId, limit, offset);

        //将查询到的所有KnowledgeDocumentDto封装成KnowledgeDocument并返回
        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentDtoList.stream().map(DtoToDomainUtil::toDocumentDomain).toList();
        return knowledgeDocumentList;
    }

    /**
     *
     * @param knowledgeCatalogId 知识库目录的ID。
     * @param limit 每页查询的数量限制。
     * @param offset 查询的起始偏移量。
     * @param keyWord 根据向量化文件名模糊查询
     * @return List<KnowledgeDocumentWithBindTime> 绑定的向量化文件列表，可能为空但不会为null。
     */
    @Override
    @NotNull
    public List<KnowledgeDocumentWithBindTime> findDocumentsWithBindInfoByCatalogId(@NotNull String knowledgeCatalogId, int limit, int offset, String keyWord) {
        // 根据 catalogId 在collection_document中查询所有相关的绑定记录
        List<CollectionDocumentDto> collectionDocumentDtoList = collectionDocumentMapper.selectByCatalogId(knowledgeCatalogId);


        // 根据documentId查询对应的document信息并将Dto封装成KnowledgeDocumentWithBindTime
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = new ArrayList<>();
        for (int i = 0; i < collectionDocumentDtoList.size(); i++) {
            KnowledgeDocumentDto knowledgeDocumentDto = knowledgeDocumentMapper.find(collectionDocumentDtoList.get(i).getDocumentId(), keyWord);
            // 如果当前查出来的对象是null，就直接跳过
            if (knowledgeDocumentDto == null){
                continue;
            }
            knowledgeDocumentWithBindTimeList.add(
                    new KnowledgeDocumentWithBindTime(
                            DtoToDomainUtil.toDocumentDomain(knowledgeDocumentDto),
                            collectionDocumentDtoList.get(i).getCreateTime()
                    )
            );
        }

        // 返回结果
        return knowledgeDocumentWithBindTimeList;
    }

    /**
     * 根据知识库ID查询可用于构建该知识库的候选文档列表。
     *
     * @param knowledgeCatalogId 知识库目录的ID。
     * @param keyWord 根据向量化文件名模糊查询
     * @return List<KnowledgeDocument> 候选向量化文件的集合，可能为空但不会为null，需要考虑去重。
     */
    @Override
    @NotNull
    public List<KnowledgeDocument> findCandidateKnowledgeDocumentKnowledgeCatalogId(@NotNull String knowledgeCatalogId, String keyWord) {
        // 根据catalogId去查询知识库类型
        KnowledgeCatalogType catalogType = knowledgeDocumentMapper.findKnowledgeCatalogType(knowledgeCatalogId);

        // 类型映射
        List<KnowledgeDocumentType> types = catalogTypeConvertToDocumentType(catalogType);

        // 查询当前知识库所有候选向量化文件
        List<KnowledgeDocumentDto> candidateDocumentList = knowledgeDocumentMapper.findCandidateDocument(knowledgeCatalogId, types, keyWord);

        // 封装成KnowledgeDocument并返回
        List<KnowledgeDocument> knowledgeDocumentList = candidateDocumentList.stream().map(DtoToDomainUtil::toDocumentDomain).toList();

        return knowledgeDocumentList;
    }


    /**
     * 新增或更新向量化文件。
     * 如果已存在相同标识的记录，则更新旧内容（upsert 操作）。
     *
     * @param knowledgeDocument 待保存的向量化文件实例。
     */
    @Override
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
        if (knowledgeDocumentMapper.find(knowledgeDocument.getId(), null) == null) {
            // 不存在,就保存
            knowledgeDocumentMapper.save(knowledgeDocumentDto);
        } else {
            // 存在，就更新
            knowledgeDocumentMapper.update(knowledgeDocumentDto);
        }
    }

    /**
     * 软删除指定的向量化文件。
     * 该操作通过更新文档的isDelete字段来标记删除，而非物理删除。
     *
     * @param knowledgeDocumentId 待删除向量化文件的ID。
     */
    @Override
    public void delete(@NotNull String knowledgeDocumentId) {
        KnowledgeDocumentDto knowledgeDocumentDto = new KnowledgeDocumentDto();
        // 对需要删除的信息封装成KnowledgeDocumentDto对象
        knowledgeDocumentDto.setDocumentId(knowledgeDocumentId);
        knowledgeDocumentDto.setIsDelete(DeleteConstant.DELETE);

        //删除对应的向量化文件，因为这里是软删除所以调用的是uodate方法
        knowledgeDocumentMapper.update(knowledgeDocumentDto);
    }

    /**
     * 根据向量化文件的ID查询所有绑定了该文件的知识库。
     *
     * @param knowledgeDocumentId 向量化文件的ID。
     * @return List<KnowledgeCatalog> 绑定了该向量化文件的知识库集合，可能为空但不会为null。
     */
    @Override
    @NotNull
    public List<KnowledgeCatalog> findKnowledgeCatalogByKnowledgeDocumentId(@NotNull String knowledgeDocumentId) {
        // 查询所有的KnowledgeCatalogDto
        List<KnowledgeCatalogDto> knowledgeCatalogDtoList = knowledgeDocumentMapper.findKnowledgeCatalogByKnowledgeDocumentId(knowledgeDocumentId);

        // 封装成KnowledgeCatalog对象返回
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeCatalogDtoList.stream().map(DtoToDomainUtil::toCatalogDomain).toList();
        return knowledgeCatalogList;
    }

    /**
     * 根据id查询对应的向量化文件
     * @param knowledgeDocumentId
     * @return KnowledgeDocument
     */
    @Override
    @NotNull
    public KnowledgeDocument findKnowledgeDocumentByKnowledgeDocumentId(@NotNull String knowledgeDocumentId) {
        KnowledgeDocumentDto knowledgeDocumentDto = knowledgeDocumentMapper.findKnowledgeDocumentByKnowledgeDocumentId(knowledgeDocumentId);
        return DtoToDomainUtil.toDocumentDomain(knowledgeDocumentDto);
    }

    /**
     * 根据向量化任务 ID 查询对应的知识文档。
     * @param embeddingMissionId 向量化任务 ID
     * @return KnowledgeDocument? 知识文档，若未查询到则返回 null
     */
    @Override
    public KnowledgeDocument findByEmbeddingMissionId(String embeddingMissionId) {
        KnowledgeDocumentDto knowledgeDocumentDto = knowledgeDocumentMapper.findByEmbedId(embeddingMissionId);
        if (knowledgeDocumentDto == null) {
            return null;
        }
        return DtoToDomainUtil.toDocumentDomain(knowledgeDocumentDto);
    }


    /**
     * 将KnowledgeCatalogType映射为对应的KnowledgeDocumentType列表。
     * 用于确定给定知识库类型所支持的文档类型。
     *
     * @param catalogType 知识库目录的类型。
     * @return List<KnowledgeDocumentType> 与知识库类型兼容的知识文档类型列表。
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
