package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    @Test
    public void FindAllTest(){
        KnowledgeCatalog knowledgeCatalog = knowledgeCatalogService.find("311f8e2a-a588-11ef-9d42-6ce2d3cf236b");
        System.out.println(knowledgeCatalog);
    }
}
