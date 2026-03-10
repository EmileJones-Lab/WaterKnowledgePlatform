package top.emilejones.hhu.knowledge.KnowledgeCatalogServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.CollectionDocumentMapper;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 测试 KnowledgeCatalogServiceImpl 的 deleteKnowledgeDocumentFromKnowledgeCatalog 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteKnowledgeDocumentFromKnowledgeCatalogTest {
    @Mock
    private KnowledgeCatalogMapper knowledgeCatalogMapper;
    @Mock
    private CollectionDocumentMapper collectionDocumentMapper;
    @InjectMocks
    private KnowledgeCatalogServiceImpl knowledgeCatalogRepository;

    /**
     * 测试批量解绑文档与知识库的功能。
     * 验证当提供有效的文档 ID 列表时，方法调用 Mapper 执行批量软删除。
     */
    @Test
    public void deleteKnowledgeDocumentFromKnowledgeCatalog() {
        String catId = UUID.randomUUID().toString();
        List<String> docIds = Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog(catId, docIds);

        Mockito.verify(collectionDocumentMapper).deleteKnowledgeDocumentFromKnowledgeCatalog(catId, docIds);
    }

    /**
     * 测试空列表解绑时的静默处理。
     * 验证当文档 ID 列表为空时，不调用 Mapper。
     */
    @Test
    public void deleteWithEmptyList() {
        knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog("any_id", Collections.emptyList());
        Mockito.verifyNoInteractions(collectionDocumentMapper);
    }
}
