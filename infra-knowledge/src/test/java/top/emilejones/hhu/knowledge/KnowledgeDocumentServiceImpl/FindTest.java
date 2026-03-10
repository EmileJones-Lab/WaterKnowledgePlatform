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
 * 测试 KnowledgeDocumentServiceImpl 的 find 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试根据 ID 查询文档。
     * 验证方法能够正确调用 Mapper 获取 DTO 并转换为领域对象。
     */
    @Test
    public void find() {
        String id = UUID.randomUUID().toString();
        KnowledgeDocumentDto dto = new KnowledgeDocumentDto();
        dto.setDocumentId(id);
        dto.setDocumentName("find.pdf");
        dto.setEmbedId(UUID.randomUUID().toString());
        dto.setCreateTime(Instant.now());
        dto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(knowledgeDocumentMapper.findKnowledgeDocumentByKnowledgeDocumentId(id))
                .thenReturn(dto);

        KnowledgeDocument result = knowledgeDocumentService.find(id);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(id, result.getId());
    }
}
