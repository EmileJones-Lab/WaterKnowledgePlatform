package top.emilejones.hhu.knowledge.KnowledgeDocumentServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.constant.DeleteConstant;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeDocumentMapper;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeCatalogDto;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 测试 KnowledgeDocumentServiceImpl 的 findKnowledgeCatalogByKnowledgeDocumentId 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindKnowledgeCatalogByKnowledgeDocumentIdTest {
    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 测试根据文档 ID 查询绑定的知识库。
     * 验证能够获取到绑定了该文档的所有知识库目录领域对象列表。
     */
    @Test
    public void findCatalogs() {
        String docId = UUID.randomUUID().toString();
        KnowledgeCatalogDto catDto = new KnowledgeCatalogDto();
        catDto.setKbId(UUID.randomUUID().toString());
        catDto.setKbName("name");
        catDto.setColName("colName");
        catDto.setType(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);
        catDto.setCreateTime(Instant.now());
        catDto.setIsDelete(DeleteConstant.EXIST);

        Mockito.when(knowledgeDocumentMapper.findKnowledgeCatalogByKnowledgeDocumentId(docId))
                .thenReturn(Collections.singletonList(catDto));

        List<KnowledgeCatalog> result = knowledgeDocumentService.findKnowledgeCatalogByKnowledgeDocumentId(docId);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(catDto.getKbId(), result.get(0).getId());
    }
}
