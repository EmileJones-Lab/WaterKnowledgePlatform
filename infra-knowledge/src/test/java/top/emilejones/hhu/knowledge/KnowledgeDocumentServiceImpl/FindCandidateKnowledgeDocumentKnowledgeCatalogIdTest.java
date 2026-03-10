package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
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
 * 测试 KnowledgeDocumentServiceImpl 的 findCandidateKnowledgeDocumentKnowledgeCatalogId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindCandidateKnowledgeDocumentKnowledgeCatalogIdTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试查询候选文档功能。
     * 验证根据知识库类型正确映射文档类型，并调用 Mapper 查询候选文件列表。
     */
    @Test
    public void findCandidate() {
        String catalogId = UUID.randomUUID().toString();
        Mockito.when(knowledgeDocumentMapper.findKnowledgeCatalogType(catalogId))
                .thenReturn(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);

        KnowledgeDocumentDto dto = new KnowledgeDocumentDto();
        dto.setDocumentId(UUID.randomUUID().toString());
        dto.setDocumentName("candidate.pdf");
        dto.setEmbedId(UUID.randomUUID().toString());
        dto.setCreateTime(Instant.now());
        dto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(knowledgeDocumentMapper.findCandidateDocument(Mockito.eq(catalogId), Mockito.anyList(), Mockito.eq("key")))
                .thenReturn(Collections.singletonList(dto));

        List<KnowledgeDocument> result = knowledgeDocumentService.findCandidateKnowledgeDocumentKnowledgeCatalogId(catalogId, "key");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dto.getDocumentId(), result.get(0).getId());
    }
}
