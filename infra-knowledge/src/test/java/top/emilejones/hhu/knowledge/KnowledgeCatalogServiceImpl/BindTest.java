package top.emilejones.hhu.knowledge.KnowledgeCatalogServiceImpl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.*;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.pojo.po.CollectionDocumentPo;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.time.Instant;
import java.util.UUID;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 bind 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class BindTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试文档与知识库目录的正常绑定功能。
     * 验证当文档类型与目录类型匹配且未绑定过时，方法正确调用 Mapper 执行绑定操作。
     */
    @Test
    public void bindSuccess() {
        String docId = UUID.randomUUID().toString();
        String catId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        KnowledgeDocument doc = Mockito.mock(KnowledgeDocument.class);
        Mockito.when(doc.getId()).thenReturn(docId);
        Mockito.when(doc.getType()).thenReturn(KnowledgeDocumentType.STRUCTURE_SPLITTER);

        KnowledgeCatalog cat = Mockito.mock(KnowledgeCatalog.class);
        Mockito.when(cat.getId()).thenReturn(catId);
        Mockito.when(cat.getType()).thenReturn(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);

        Mockito.when(collectionDocumentMapper.selectFromCollectionDocument(docId, catId)).thenReturn(0);

        knowledgeCatalogRepository.bind(doc, cat, now);

        ArgumentCaptor<CollectionDocumentPo> captor = ArgumentCaptor.forClass(CollectionDocumentPo.class);
        Mockito.verify(collectionDocumentMapper).bind(captor.capture());
        Assertions.assertEquals(docId, captor.getValue().getDocumentId());
        Assertions.assertEquals(catId, captor.getValue().getKbId());
    }

    /**
     * 测试类型不匹配时的绑定验证。
     * 验证当文档切割类型与知识库类型不兼容时，抛出 IllegalStateException。
     */
    @Test
    public void bindTypeMismatch() {
        KnowledgeDocument doc = Mockito.mock(KnowledgeDocument.class);
        Mockito.when(doc.getType()).thenReturn(KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200);

        KnowledgeCatalog cat = Mockito.mock(KnowledgeCatalog.class);
        Mockito.when(cat.getType()).thenReturn(KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            knowledgeCatalogRepository.bind(doc, cat, Instant.now());
        });
    }
}
