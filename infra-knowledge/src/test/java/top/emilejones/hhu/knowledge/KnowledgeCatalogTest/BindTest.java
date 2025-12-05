package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.controller.KnowledgeCatalogController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
public class BindTest {
    @Autowired
    private KnowledgeCatalogController knowledgeCatalogController;

    @Test
    public void bindTest(){
        KnowledgeDocumentType type = KnowledgeDocumentType.STRUCTURE_SPLITTER;
        Instant createTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        KnowledgeDocument knowledgeDocument = new KnowledgeDocument("684f71bb-278f-4675-aa17-a14e6274b735", "test", "12886c1b-2fe0-4206-9229-2bb68a0e4086", type, createTime);


        KnowledgeCatalog knowledgeCatalog = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test3",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        knowledgeCatalogController.bind(knowledgeDocument, knowledgeCatalog);

    }
}
