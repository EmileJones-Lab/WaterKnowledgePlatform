package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.time.Instant;
import java.util.UUID;

/**
 * 测试 KnowledgeDocumentServiceImpl 的 findByEmbeddingMissionId 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByEmbeddingMissionIdTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试根据向量化任务 ID 查询文档。
     * 验证能够通过任务 ID 获取对应的文档领域对象，处理存在及不存在的情况。
     */
    @Test
    public void findByEmbedId() {
        String embedId = UUID.randomUUID().toString();
        KnowledgeDocumentDto dto = new KnowledgeDocumentDto();
        dto.setEmbedId(embedId);
        dto.setDocumentId(UUID.randomUUID().toString());
        dto.setDocumentName("documentName");
        dto.setCreateTime(Instant.now());
        dto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(knowledgeDocumentMapper.findByEmbedId(embedId)).thenReturn(dto);

        KnowledgeDocument result = knowledgeDocumentService.findByEmbeddingMissionId(embedId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(dto.getDocumentId(), result.getId());
        
        // 测试不存在的情况
        Mockito.when(knowledgeDocumentMapper.findByEmbedId("non_exist")).thenReturn(null);
        Assertions.assertNull(knowledgeDocumentService.findByEmbeddingMissionId("non_exist"));
    }
}
