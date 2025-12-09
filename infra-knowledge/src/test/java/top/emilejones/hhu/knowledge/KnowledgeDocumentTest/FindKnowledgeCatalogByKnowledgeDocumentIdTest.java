package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class FindKnowledgeCatalogByKnowledgeDocumentIdTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    /**
     * 字符切分的知识库
     */
    @Test
    public void findKnowledgeCatalogByCharKnowledgeDocumentIdTest(){
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeDocumentService.findKnowledgeCatalogByKnowledgeDocumentId("82323976-0a04-475d-a505-e26b629d4f31");

        for (int i = 0; i <knowledgeCatalogList.size(); i++) {
            System.out.println(knowledgeCatalogList.get(i));
        }
    }

    /**
     * 结构切分的知识库
     */
    @Test
    public void findKnowledgeCatalogByStructureKnowledgeDocumentIdTest(){
        List<KnowledgeCatalog> knowledgeCatalogList = knowledgeDocumentService.findKnowledgeCatalogByKnowledgeDocumentId("1e81b016-e564-493b-953c-56e80389e998");

        for (int i = 0; i <knowledgeCatalogList.size(); i++) {
            System.out.println(knowledgeCatalogList.get(i));
        }
    }
}
