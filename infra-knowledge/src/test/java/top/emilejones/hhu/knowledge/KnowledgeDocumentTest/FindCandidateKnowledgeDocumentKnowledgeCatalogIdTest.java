package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class FindCandidateKnowledgeDocumentKnowledgeCatalogIdTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Test
    public void  findCandidateKnowledgeDocumentKnowledgeCatalogIdTest(){
        // 测试结构
//        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentService.findCandidateKnowledgeDocumentKnowledgeCatalogId("311f8e2a-a588-11ef-9d42-6ce2d3cf236c");
//
//        System.out.println(knowledgeDocumentList);

        // 测试字符
        List<KnowledgeDocument> knowledgeDocumentList = knowledgeDocumentService.findCandidateKnowledgeDocumentKnowledgeCatalogId("649b8aa0-c44d-4427-bb07-fed70a16dfd3");

        System.out.println(knowledgeDocumentList);

    }
}
