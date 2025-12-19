package top.emilejones.hhu.knowledge.KnowledgeCatalogTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalogType;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeCatalogServiceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest(classes = TestApplication.class)
public class BindTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    @Test()
    public void bindStructureToStructureTest(){

        KnowledgeDocument knowledgeDocumentStructure = new KnowledgeDocument(
                "684f71bb-278f-4675-aa17-a14e6274b735",
                "test19",
                "8a033c81-4121-431a-87b0-39f4508e45aa",
                KnowledgeDocumentType.STRUCTURE_SPLITTER,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogStructure = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR
        );

        // 结构绑结构
        knowledgeCatalogService.bind(knowledgeDocumentStructure, knowledgeCatalogStructure);

    }

    @Test()
    public void bindCharToStructureTest(){

        KnowledgeDocument knowledgeDocumentChar = new KnowledgeDocument(
                "12061c96-3527-4141-91b1-25bd9cf3ce80",
                "test19",
                "8a033c81-4121-431a-87b0-39f4508e45aa",
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogStructure = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR
        );

        // 字符绑结构
        knowledgeCatalogService.bind(knowledgeDocumentChar, knowledgeCatalogStructure);

    }

    @Test()
    public void bindStructureToCharTest(){

        KnowledgeCatalog knowledgeCatalogChar = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        KnowledgeDocument knowledgeDocumentStructure = new KnowledgeDocument(
                "684f71bb-278f-4675-aa17-a14e6274b735",
                "test19",
                "8a033c81-4121-431a-87b0-39f4508e45aa",
                KnowledgeDocumentType.STRUCTURE_SPLITTER,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        // 结构绑字符
        knowledgeCatalogService.bind(knowledgeDocumentStructure, knowledgeCatalogChar);

    }

    @Test()
    public void bindCharToCharTest(){

        KnowledgeDocument knowledgeDocumentChar = new KnowledgeDocument(
                "12061c96-3527-4141-91b1-25bd9cf3ce80",
                "test19",
                "8a033c81-4121-431a-87b0-39f4508e45aa",
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogChar = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        // 字符绑字符
        knowledgeCatalogService.bind(knowledgeDocumentChar, knowledgeCatalogChar);

    }
}
