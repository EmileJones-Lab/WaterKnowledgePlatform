package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;
    @Test
    public void saveTest(){
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                UUID.randomUUID().toString(),
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
                );

        knowledgeCatalogService.save(knowledgeCatalog);
    }

    @Test
    public void updateTest(){
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test5",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        knowledgeCatalogService.save(knowledgeCatalog);
    }
}
