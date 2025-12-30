package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    @Test
    public void deleteTest(){
        knowledgeCatalogService.delete("649b8aa0-c44d-4427-bb07-fed70a16dfd3");
    }
}
