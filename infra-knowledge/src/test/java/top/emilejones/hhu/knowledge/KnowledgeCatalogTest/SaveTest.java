package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.controller.KnowledgeCatalogController;

@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Autowired
    private KnowledgeCatalogController knowledgeCatalogController;
    @Test
    public void saveTest(){
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test3",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
                );

        knowledgeCatalogController.save(knowledgeCatalog);
    }
}
