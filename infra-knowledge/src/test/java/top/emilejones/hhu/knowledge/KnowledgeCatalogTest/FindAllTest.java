package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.util.List;

@SpringBootTest(classes = TestApplication.class)
public class FindAllTest {

    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    @Test
    public void FindAllTest(){
        List<KnowledgeCatalog> all = knowledgeCatalogService.findAll();
        for (int i = 0; i < all.size(); i++) {
            System.out.println(all.get(i));
        }
    }
}
