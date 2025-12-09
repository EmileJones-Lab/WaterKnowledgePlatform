package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class FindByKnowledgeCatalogIdTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Test
    public void findByKnowledgeCatalogIdTest(){
        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentService.findByKnowledgeCatalogId(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                5,
                5
        );

        System.out.println(knowledgeDocumentList);
    }
}
