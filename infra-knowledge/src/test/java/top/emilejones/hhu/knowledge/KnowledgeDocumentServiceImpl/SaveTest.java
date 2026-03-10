package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

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
 * 测试 KnowledgeDocumentServiceImpl 的 save 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试新增文档保存。
     * 验证当文档不存在时调用 Mapper 的 save 方法。
     */
    @Test
    public void saveNew() {
        String id = UUID.randomUUID().toString();
        KnowledgeDocument doc = Mockito.mock(KnowledgeDocument.class);
        Mockito.when(doc.getId()).thenReturn(id);

        Mockito.when(knowledgeDocumentMapper.find(id, null)).thenReturn(null);

        knowledgeDocumentService.save(doc);

        Mockito.verify(knowledgeDocumentMapper).save(Mockito.any(KnowledgeDocumentDto.class));
    }

    /**
     * 测试更新现有文档。
     * 验证当文档已存在时调用 Mapper 的 update 方法。
     */
    @Test
    public void saveUpdate() {
        String id = UUID.randomUUID().toString();
        KnowledgeDocument doc = Mockito.mock(KnowledgeDocument.class);
        Mockito.when(doc.getId()).thenReturn(id);

        KnowledgeDocumentDto existingDto = new KnowledgeDocumentDto();
        existingDto.setDocumentId(id);
        existingDto.setDocumentName("old.pdf");
        existingDto.setEmbedId(UUID.randomUUID().toString());
        existingDto.setCreateTime(Instant.now());
        existingDto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(knowledgeDocumentMapper.find(id, null)).thenReturn(existingDto);

        knowledgeDocumentService.save(doc);

        Mockito.verify(knowledgeDocumentMapper).update(Mockito.any(KnowledgeDocumentDto.class));
    }
}
