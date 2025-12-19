package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

@SpringBootTest(classes = TestApplication.class)
public class FindByEmbeddingMissionIdTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Test
    public void findByEmbeddingMissionIdTest() {
        // 使用一个示例的 embeddingMissionId 进行测试（参考 SaveTest 中的数据）
        KnowledgeDocument knowledgeDocument = knowledgeDocumentService.findByEmbeddingMissionId("987c57ac-2113-4b54-a4b6-fe6bd4f5d984");

        System.out.println(knowledgeDocument);
    }
}
