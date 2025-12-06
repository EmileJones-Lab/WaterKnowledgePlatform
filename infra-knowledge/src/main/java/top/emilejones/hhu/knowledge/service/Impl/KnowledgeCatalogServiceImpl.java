package top.emilejones.hhu.knowledge.service.Impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeCatalogRepository;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.po.CollectionDocumentPo;

import java.time.*;
import java.util.EnumSet;
import java.util.List;

/**
 * KnowLedgeCatalog业务层，完成业务上的逻辑
 * @author EmileNathon
 */
@Service
public class KnowledgeCatalogServiceImpl implements KnowledgeCatalogRepository {
    @Autowired
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    /**
     * 查询所有的知识库信息
     * @return List<KnowledgeCatalog> 不能为null，可以是空
     */
    public @NotNull List<KnowledgeCatalog> findAll() {
        // 从数据库中查询所有的知识库信息
        List<KnowledgeCatalogDto> knowledgeCatalogDtoList = knowledgeCatalogMapper.findAll();

        // 封装成KnowledgeCatalog并返回
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeCatalogDtoList.stream().map(this::toDomain).toList();
        return knowledgeCatalogList;
    }

    /**
     * 根据id查询知识库信息
     * @param knowledgeCatalogId
     * @return KnowledgeCatalog
     */
    @Nullable
    public KnowledgeCatalog find(@NotNull String knowledgeCatalogId) {
        // 根据id查询知识库信息
        KnowledgeCatalogDto knowledgeCatalogDto = knowledgeCatalogMapper.find(knowledgeCatalogId);

        // 封装成KnowledgeCatalog并返回
        KnowledgeCatalog knowledgeCatalog = toDomain(knowledgeCatalogDto);
        return knowledgeCatalog;
    }

    /**
     * 新增一个知识库，如果存在就更新记录
     * @param knowledgeCatalog
     */
    public void save(@NotNull KnowledgeCatalog knowledgeCatalog) {
        // 将KnoledgeCatalog封装成Dto
        KnowledgeCatalogDto knowledgeCatalogDto = new KnowledgeCatalogDto();
        knowledgeCatalogDto.setKbId(knowledgeCatalog.getId());
        knowledgeCatalogDto.setKbName(knowledgeCatalog.getName());
        knowledgeCatalogDto.setColName(knowledgeCatalog.getMilvusCollectionName());
        knowledgeCatalogDto.setType(knowledgeCatalog.getType());

        // 设置创建的时间和知识库权限(默认就是public)
        knowledgeCatalogDto.setCreateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        knowledgeCatalogDto.setPermission("public");

        // 判断在数据库中是否存在
        if (knowledgeCatalogMapper.find(knowledgeCatalog.getId()) == null){
            // 不存在就保存
            knowledgeCatalogMapper.save(knowledgeCatalogDto);
        }else {
            // 存在就更新
            knowledgeCatalogMapper.update(knowledgeCatalogDto);
        }

    }

    /**
     * 将向量化后的文件与知识库绑定
     * @param knowledgeDocument
     * @param knowledgeCatalog
     */
    public void bind(@NotNull KnowledgeDocument knowledgeDocument, @NotNull KnowledgeCatalog knowledgeCatalog) {
        // 获取知识库id和向量化文件id
        String catalogId = knowledgeCatalog.getId();
        String documentId = knowledgeDocument.getId();

        // 判断向量化文件的type和知识库的type是否一致
        validateDocumentAndCatalogType(knowledgeDocument, knowledgeCatalog);

        // 判断是否已经绑定
        if (isBind(documentId, catalogId)){
            // 已经绑定就报错
            return;
        }

        // 将相关信息封装成collectionDocumentPo对象
        CollectionDocumentPo collectionDocumentPo = new CollectionDocumentPo();
        collectionDocumentPo.setKbId(catalogId);
        collectionDocumentPo.setDocumentId(documentId);

        // 设置创建的时间和删除的标识，默认未删除
        collectionDocumentPo.setCreateTime(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        collectionDocumentPo.setIsDelete(DeleteConstant.EXIST);

        // 存入数据库
        knowledgeCatalogMapper.bind(collectionDocumentPo);
    }

    private boolean isBind(String documentId, String catalogId) {
        if (knowledgeCatalogMapper.selectFromCollectionDocument(documentId, catalogId) > 0){
            return true;
        }
        return false;
    }

    /**
     * 将knowledgeCatalogDto封装成KnowledgeCatalog
     * @param knowledgeCatalogDto
     * @return KnowledgeCatalog
     */
    private KnowledgeCatalog toDomain(KnowledgeCatalogDto knowledgeCatalogDto){
        return new KnowledgeCatalog(
                knowledgeCatalogDto.getKbId(),
                knowledgeCatalogDto.getKbName(),
                knowledgeCatalogDto.getColName(),
                knowledgeCatalogDto.getType()
        );
    }

    /**
     * 根据知识文档切割方式绑定到对应的知识库中，需要配对绑定
     * @param knowledgeDocument 知识文档
     * @param knowledgeCatalog 知识库
     */
    private void validateDocumentAndCatalogType(@NotNull KnowledgeDocument knowledgeDocument, @NotNull KnowledgeCatalog knowledgeCatalog) {
        KnowledgeDocumentType documentType = knowledgeDocument.getType();
        KnowledgeCatalogType catalogType = knowledgeCatalog.getType();
        EnumSet<KnowledgeDocumentType> charTypes = EnumSet.of(
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200,
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_400,
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_600
        );

        // 结构切割的文件绑定到非结构知识库
        if (documentType == KnowledgeDocumentType.STRUCTURE_SPLITTER && catalogType != KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR) {
            throw new IllegalArgumentException("根据文本结构切割的文件必须绑定到基于文本层次结构的知识库");
        }

        // 字符切割的文件绑定到非字符知识库
        if (charTypes.contains(documentType) && catalogType != KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR) {
            throw new IllegalArgumentException("字符长度切割类型的文件必须绑定到字符长度切割的知识库");
        }

        // 未知的切割类型
        if (!charTypes.contains(documentType) && documentType != KnowledgeDocumentType.STRUCTURE_SPLITTER) {
            throw new IllegalArgumentException("未知的知识文档类型，无法绑定");
        }
    }
}
