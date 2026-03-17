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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 测试 KnowledgeDocumentServiceImpl 的 findByKnowledgeCatalogId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByKnowledgeCatalogIdTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试根据知识库 ID 分页查询已绑定文档的功能后。
     * 验证方法能否通过 Mapper 获取文档 DTO 列表并正确转换为领域对象列表。
     */
    @Test
    public void findByKnowledgeCatalogId() {
        String catalogId = UUID.randomUUID().toString();
        KnowledgeDocumentDto dto = new KnowledgeDocumentDto();
        dto.setDocumentId(UUID.randomUUID().toString());
        dto.setDocumentName("test.pdf");
        dto.setEmbedId(UUID.randomUUID().toString());
        dto.setCreateTime(Instant.now());
        dto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(knowledgeDocumentMapper.findByKnowledgeCatalogId(catalogId, 10, 0))
                .thenReturn(Collections.singletonList(dto));

        List<KnowledgeDocument> result = knowledgeDocumentService.findByKnowledgeCatalogId(catalogId, 10, 0);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getDocumentId(), result.get(0).getId());
    }
}
