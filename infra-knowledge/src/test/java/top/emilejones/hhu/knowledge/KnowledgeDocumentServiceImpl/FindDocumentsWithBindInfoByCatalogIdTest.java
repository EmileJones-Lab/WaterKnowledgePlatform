package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.infrastructure.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.CollectionDocumentDto;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 测试 KnowledgeDocumentServiceImpl 的 findDocumentsWithBindInfoByCatalogId 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindDocumentsWithBindInfoByCatalogIdTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试根据知识库 ID 查询带有绑定信息的文档列表。
     * 验证方法能够整合来自不同 Mapper 的绑定时间与文档详情数据。
     */
    @Test
    public void findDocumentsWithBindInfo() {
        String catalogId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        CollectionDocumentDto bindDto = new CollectionDocumentDto();
        bindDto.setDocumentId(docId);
        bindDto.setCreateTime(now);

        KnowledgeDocumentDto docDto = new KnowledgeDocumentDto();
        docDto.setDocumentId(docId);
        docDto.setDocumentName("test.docx");
        docDto.setEmbedId(UUID.randomUUID().toString());
        docDto.setCreateTime(now);
        docDto.setType(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        Mockito.when(collectionDocumentMapper.selectByCatalogId(catalogId))
                .thenReturn(Collections.singletonList(bindDto));
        Mockito.when(knowledgeDocumentMapper.find(docId, "key"))
                .thenReturn(docDto);

        List<KnowledgeDocumentWithBindTime> result = knowledgeDocumentService.findDocumentsWithBindInfoByCatalogId(catalogId, 10, 0, "key");

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(now, result.get(0).getBindTime());
        Assertions.assertEquals(docId, result.get(0).getKnowledgeDocument().getId());
    }
}
