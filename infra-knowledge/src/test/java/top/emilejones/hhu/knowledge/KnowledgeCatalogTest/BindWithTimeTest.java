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

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = TestApplication.class)
public class BindWithTimeTest {
    @Autowired
    private KnowledgeCatalogServiceImpl knowledgeCatalogService;

    /**
     * 结构文档绑定结构库，携带自定义绑定时间
     */
    @Test
    public void bindStructureToStructureWithTimeTest() {
        KnowledgeDocument knowledgeDocumentStructure = new KnowledgeDocument(
                "beeae76e-22e9-48e5-921d-34f59c3700dc",
                "abcd",
                "f66ed2f6-a923-4d18-a392-47f5cd588e0c",
                KnowledgeDocumentType.STRUCTURE_SPLITTER,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogStructure = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test3",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR
        );

        Instant bindTime = Instant.now();

        knowledgeCatalogService.bind(knowledgeDocumentStructure, knowledgeCatalogStructure, bindTime);
    }

    /**
     * 字符文档绑定结构库（类型不匹配，预期抛异常）
     */
    @Test
    public void bindCharToStructureWithTimeTest() {
        KnowledgeDocument knowledgeDocumentChar = new KnowledgeDocument(
                "12061c96-3527-4141-91b1-25bd9cf3ce80",
                "test19",
                "8a033c81-4121-431a-87b0-39f4508e45aa",
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_200,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogStructure = new KnowledgeCatalog(
                "311f8e2a-a588-11ef-9d42-6ce2d3cf236c",
                "test3",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.STRUCTURE_KNOWLEDGE_DIR
        );

        Instant bindTime = Instant.now();
        knowledgeCatalogService.bind(knowledgeDocumentChar, knowledgeCatalogStructure, bindTime);
    }

    /**
     * 结构文档绑定字符库（类型不匹配，预期抛异常）
     */
    @Test
    public void bindStructureToCharWithTimeTest() {
        KnowledgeDocument knowledgeDocumentStructure = new KnowledgeDocument(
                "beeae76e-22e9-48e5-921d-34f59c3700dc",
                "abcd",
                "f66ed2f6-a923-4d18-a392-47f5cd588e0c",
                KnowledgeDocumentType.STRUCTURE_SPLITTER,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogChar = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        Instant bindTime = Instant.now();

        knowledgeCatalogService.bind(knowledgeDocumentStructure, knowledgeCatalogChar, bindTime);
    }

    /**
     * 字符文档绑定字符库，携带自定义绑定时间
     */
    @Test
    public void bindCharToCharWithTimeTest() {
        KnowledgeDocument knowledgeDocumentChar = new KnowledgeDocument(
                "82323976-0a04-475d-a505-e26b629d4f31",
                "badc",
                "df304f2f-2092-4b95-b7fc-dd40270c5762",
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_600,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogChar = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        Instant bindTime = Instant.now();

        knowledgeCatalogService.bind(knowledgeDocumentChar, knowledgeCatalogChar, bindTime);
    }

    /**
     * 已经绑定不做任何操作
     */
    @Test
    public void hadBindTest() {
        KnowledgeDocument knowledgeDocumentChar = new KnowledgeDocument(
                "419ddefd-04b1-485f-a5c4-15b25ef0f7d0",
                "test12",
                "262e5349-f2fd-4d7b-b2a5-982d9d437dc9",
                KnowledgeDocumentType.CHAR_LENGTH_SPLITTER_600,
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );

        KnowledgeCatalog knowledgeCatalogChar = new KnowledgeCatalog(
                "649b8aa0-c44d-4427-bb07-fed70a16dfd3",
                "test4",
                "_30cbe64e_a588_11ef_9d42_6ce2d3cf236c",
                java.time.Instant.now(),
                KnowledgeCatalogType.CHAR_NUMBER_SPLIT_DIR
        );

        Instant bindTime = Instant.now();

        knowledgeCatalogService.bind(knowledgeDocumentChar, knowledgeCatalogChar, bindTime);
    }
}
