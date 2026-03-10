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
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.po.CollectionDocumentPo;
import top.emilejones.hhu.knowledge.utils.DtoToDomainUtil;

import java.time.*;
import java.util.EnumSet;
import java.util.List;

/**
 * KnowledgeCatalogServiceImpl 是 KnowledgeCatalogRepository 接口的实现类，
 * 负责处理知识库目录相关的业务逻辑操作。
 * 它通过与 KnowledgeCatalogMapper 交互来持久化和检索知识库目录数据。
 * @author EmileNathon
 */
@Service
public class KnowledgeCatalogServiceImpl implements KnowledgeCatalogRepository {
    @Autowired
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Autowired
    private CollectionDocumentMapper collectionDocumentMapper;

    @NotNull
    @Override
    public List<KnowledgeCatalog> findAll() {
        // 从数据库中查询所有的知识库信息
        List<KnowledgeCatalogDto> knowledgeCatalogDtoList = knowledgeCatalogMapper.findAll();

        // 封装成KnowledgeCatalog并返回
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeCatalogDtoList.stream()
                .filter(dto -> dto.getIsDelete() != 0)
                .map(DtoToDomainUtil::toCatalogDomain)
                .toList();
        return knowledgeCatalogList;
    }

    /**
     * 根据id查询知识库信息。
     * @param knowledgeCatalogId 知识库目录的唯一标识符。
     * @return KnowledgeCatalog 知识库目录，如果不存在则返回null。
     */
    @Nullable
    @Override
    public KnowledgeCatalog find(@NotNull String knowledgeCatalogId) {
        // 根据id查询知识库信息
        KnowledgeCatalogDto knowledgeCatalogDto = knowledgeCatalogMapper.find(knowledgeCatalogId);

        if (knowledgeCatalogDto == null || knowledgeCatalogDto.getIsDelete() == 0) {
            return null;
        }

        // 封装成KnowledgeCatalog并返回
        KnowledgeCatalog knowledgeCatalog = DtoToDomainUtil.toCatalogDomain(knowledgeCatalogDto);
        return knowledgeCatalog;
    }

    /**
     * 新增一个知识库；如果已存在相同标识的记录，则更新旧内容（upsert 操作）。
     * @param knowledgeCatalog 待保存的知识库目录实例。
     */
    @Override
    public void save(@NotNull KnowledgeCatalog knowledgeCatalog) {
        // 将KnoledgeCatalog封装成Dto
        KnowledgeCatalogDto knowledgeCatalogDto = new KnowledgeCatalogDto();
        knowledgeCatalogDto.setKbId(knowledgeCatalog.getId());
        knowledgeCatalogDto.setKbName(knowledgeCatalog.getName());
        knowledgeCatalogDto.setColName(knowledgeCatalog.getMilvusCollectionName());
        knowledgeCatalogDto.setCreateTime(knowledgeCatalog.getCreateTime());
        knowledgeCatalogDto.setType(knowledgeCatalog.getType());
        knowledgeCatalogDto.setIsDelete(1);

        // 设置知识库权限(默认就是public)
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
     * 将一个向量化后的文件与指定的知识库绑定。
     * 如果已经绑定，则不进行任何操作。
     * @param knowledgeDocument 待绑定的知识文档。
     * @param knowledgeCatalog 目标知识库。
     * @param bindTime 绑定的时间。
     */
    @Override
    public void bind(@NotNull KnowledgeDocument knowledgeDocument, @NotNull KnowledgeCatalog knowledgeCatalog, @NotNull Instant bindTime) {
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
        collectionDocumentPo.setCreateTime(bindTime);
        collectionDocumentPo.setIsDelete(DeleteConstant.EXIST);

        // 存入数据库
        collectionDocumentMapper.bind(collectionDocumentPo);
    }

    /**
     * 软删除指定的知识库。
     * 该操作通过更新知识库的isDelete字段为指定删除状态来标记删除，而非物理删除。
     * @param knowledgeCatalogId 待删除知识库的ID。
     */
    @Override
    public void delete(@NotNull String knowledgeCatalogId) {
        // 将信息封装成KnowledgeCatalogDto对象
        KnowledgeCatalogDto knowledgeCatalogDto = new KnowledgeCatalogDto();
        knowledgeCatalogDto.setKbId(knowledgeCatalogId);
        knowledgeCatalogDto.setIsDelete(DeleteConstant.DELETE);

        // 删除当前知识库，这里是软删除所以就是更新数据库的isdelete字段的值
        knowledgeCatalogMapper.update(knowledgeCatalogDto);
    }

    /**
     * 批量解绑指定知识库中的向量化文件。
     * 这实质上是一个“unbind”操作，通过软删除（更新collection_document的isDelete字段）来解除绑定。
     * @param knowledgeCatalogId 知识库的ID。
     * @param knowledgeDocumentIdList 待解绑的向量化文件ID列表。
     */
    @Override
    public void deleteKnowledgeDocumentFromKnowledgeCatalog(@NotNull String knowledgeCatalogId, @NotNull List<String> knowledgeDocumentIdList) {
        // 静默处理
        if (knowledgeDocumentIdList.isEmpty()){
            return;
        }

        // 进行解绑，软删除操作，实质就是更新collection_document的isdelete字段
        collectionDocumentMapper.deleteKnowledgeDocumentFromKnowledgeCatalog(knowledgeCatalogId, knowledgeDocumentIdList);
    }

    /**
     * 判断指定的知识文档是否已经绑定到知识库。
     * @param documentId 知识文档的ID。
     * @param catalogId 知识库的ID。
     * @return boolean 如果已绑定则返回true，否则返回false。
     */
    private boolean isBind(String documentId, String catalogId) {
        if (collectionDocumentMapper.selectFromCollectionDocument(documentId, catalogId) > 0){
            return true;
        }
        return false;
    }


    /**
     * 验证知识文档的切割方式与目标知识库的类型是否匹配。
     * 如果类型不匹配，则抛出 IllegalArgumentException。
     * @param knowledgeDocument 待验证的知识文档。
     * @param knowledgeCatalog 待验证的目标知识库。
     * @throws IllegalStateException 如果文档类型和知识库类型不兼容。
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
            throw new IllegalStateException("根据文本结构切割的文件必须绑定到基于文本层次结构的知识库");
        }

        // 字符切割的文件绑定到非字符知识库
        if (charTypes.contains(documentType) && catalogType != KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR) {
            throw new IllegalStateException("字符长度切割类型的文件必须绑定到字符长度切割的知识库");
        }

        // 未知的切割类型
        if (!charTypes.contains(documentType) && documentType != KnowledgeDocumentType.STRUCTURE_SPLITTER) {
            throw new IllegalStateException("未知的知识文档类型，无法绑定");
        }
    }


}
