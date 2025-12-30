package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.infrastructure.dto.KnowledgeDocumentWithBindTime;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class FindDocumentsWithBindInfoByCatalogIdTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Test
    public void findDocumentsWithBindInfoByCatalogIdStructureTest() {
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = knowledgeDocumentService
                .findDocumentsWithBindInfoByCatalogId("311f8e2a-a588-11ef-9d42-6ce2d3cf236c", 10, 0, null);

        for (KnowledgeDocumentWithBindTime knowledgeDocumentWithBindTime : knowledgeDocumentWithBindTimeList) {
            System.out.println(knowledgeDocumentWithBindTime.getKnowledgeDocument());
            System.out.println(knowledgeDocumentWithBindTime.getBindTime());
        }
    }

    @Test
    public void findDocumentsWithBindInfoByCatalogIdCharacterTest() {
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = knowledgeDocumentService
                .findDocumentsWithBindInfoByCatalogId("649b8aa0-c44d-4427-bb07-fed70a16dfd3", 10, 0, null);

        for (KnowledgeDocumentWithBindTime knowledgeDocumentWithBindTime : knowledgeDocumentWithBindTimeList) {
            System.out.println(knowledgeDocumentWithBindTime.getKnowledgeDocument());
            System.out.println(knowledgeDocumentWithBindTime.getBindTime());
        }
    }

    @Test
    public void findDocumentsWithBindInfoAndNameByCatalogIdStructureTest() {
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = knowledgeDocumentService
                .findDocumentsWithBindInfoByCatalogId("311f8e2a-a588-11ef-9d42-6ce2d3cf236c", 10, 0, "ab");

        for (KnowledgeDocumentWithBindTime knowledgeDocumentWithBindTime : knowledgeDocumentWithBindTimeList) {
            System.out.println(knowledgeDocumentWithBindTime.getKnowledgeDocument());
            System.out.println(knowledgeDocumentWithBindTime.getBindTime());
        }
    }

    @Test
    public void findDocumentsWithBindInfoAndNameByCatalogIdCharacterTest() {
        List<KnowledgeDocumentWithBindTime> knowledgeDocumentWithBindTimeList = knowledgeDocumentService
                .findDocumentsWithBindInfoByCatalogId("649b8aa0-c44d-4427-bb07-fed70a16dfd3", 10, 0, "bad");

        for (KnowledgeDocumentWithBindTime knowledgeDocumentWithBindTime : knowledgeDocumentWithBindTimeList) {
            System.out.println(knowledgeDocumentWithBindTime.getKnowledgeDocument());
            System.out.println(knowledgeDocumentWithBindTime.getBindTime());
        }
    }
}
