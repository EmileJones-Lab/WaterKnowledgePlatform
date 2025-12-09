package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class DeleteKnowledgeDocumentFromKnowledgeCatalogTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    /**
     * 字符切割
     */
    @Test
    public void deleteKnowledgeDocumentFromCharKnowledgeCatalogTest(){
        List<String> documentIdList = new ArrayList<>();
        documentIdList.add("beeae76e-22e9-48e5-921d-34f59c3700dc");
        documentIdList.add("32d6122d-e76c-4ec0-af72-7c6ae07f0711");
        documentIdList.add("1568b4de-7683-4246-8967-ac217424ea13");

        knowledgeCatalogService.deleteKnowledgeDocumentFromKnowledgeCatalog("311f8e2a-a588-11ef-9d42-6ce2d3cf236c", documentIdList);
    }


    @Test
    public void deleteKnowledgeDocumentFromStructureKnowledgeCatalogTest(){
        List<String> documentIdList = new ArrayList<>();
        documentIdList.add("12061c96-3527-4141-91b1-25bd9cf3ce80");
        documentIdList.add("289662e6-c343-47ce-bf6b-c1df8321e34a");
        documentIdList.add("0ba1c46e-9455-429c-a8c3-09fdcf6f250f");

        knowledgeCatalogService.deleteKnowledgeDocumentFromKnowledgeCatalog("649b8aa0-c44d-4427-bb07-fed70a16dfd3", documentIdList);
    }
}
