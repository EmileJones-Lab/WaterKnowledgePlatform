package top.emilejones.hhu.knowledge.service.Impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;

import java.util.List;

@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentRepository {
    @Autowired
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Override
    public @NotNull List<KnowledgeDocument> findByKnowledgeCatalogId(@NotNull String knowledgeCatalogId, int limit, int offset) {
        return List.of();
    }

    @Override
    @NotNull
    public List<KnowledgeDocument> findCandidateKnowledgeDocumentKnowledgeCatalogId(@NotNull String knowledgeCatalogId) {
        return List.of();
    }

    /**
     * 新增向量化文件，如果存在就更新
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

    @Override
    public void delete(@NotNull String knowledgeDocumentId) {

    }
}
