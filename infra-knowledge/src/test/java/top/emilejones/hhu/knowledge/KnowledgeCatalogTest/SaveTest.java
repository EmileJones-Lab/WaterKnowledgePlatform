package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.mapper.KnowledgeCatalogMapper;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    private static KnowledgeCatalogServiceImpl knowledgeCatalogService;
    private static KnowledgeCatalogMapper knowledgeCatalogMapper;

    @Autowired
    public void setKnowledgeCatalogService(KnowledgeCatalogServiceImpl knowledgeCatalogService) {
        SaveTest.knowledgeCatalogService = knowledgeCatalogService;
    }

    @Autowired
    public void setKnowledgeCatalogMapper(KnowledgeCatalogMapper knowledgeCatalogMapper) {
        SaveTest.knowledgeCatalogMapper = knowledgeCatalogMapper;
    }

    private static List<String> idList = new ArrayList<>();

    @AfterAll
    public static void after(){
        idList.forEach(id -> knowledgeCatalogMapper.hardDelete(id));
    }

    @Test
    public void saveTest(){
        String id = UUID.randomUUID().toString();
        idList.add(id);
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                id,
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
                );

        knowledgeCatalogService.save(knowledgeCatalog);
    }

    @Test
    public void updateTest(){
        String id = "649b8aa0-c44d-4427-bb07-fed70a16dfd3";
        idList.add(id);
        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                id,
                "test5",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        knowledgeCatalogService.save(knowledgeCatalog);
    }
}
