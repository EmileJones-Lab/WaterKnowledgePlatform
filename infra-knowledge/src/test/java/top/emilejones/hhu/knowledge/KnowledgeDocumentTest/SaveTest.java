package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.service.Impl.KnowledgeDocumentServiceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Autowired
    private KnowledgeDocumentServiceImpl knowledgeDocumentService;

    @Test
    public void saveTest(){
        for (int i = 0; i < 20; i++) {
            String documentId = String.valueOf(UUID.randomUUID());
            String name = "test" + i;
            String embeddingMissionId = String.valueOf(UUID.randomUUID());
            KnowledgeDocumentType type = KnowledgeDocumentType.STRUCTURE_SPLITTER;
            Instant createTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            KnowledgeDocument knowledgeDocument = new KnowledgeDocument(documentId, name, embeddingMissionId, type, createTime);
            knowledgeDocumentService.save(knowledgeDocument);
        }

    }

    @Test
    public void updateTest(){
            String documentId = "684f71bb-278f-4675-aa17-a14e6274b735";
            String name = "test";
            String embeddingMissionId = "987c57ac-2113-4b54-a4b6-fe6bd4f5d984";
            KnowledgeDocumentType type = KnowledgeDocumentType.STRUCTURE_SPLITTER;
            Instant createTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            KnowledgeDocument knowledgeDocument = new KnowledgeDocument(documentId, name, embeddingMissionId, type, createTime);
            knowledgeDocumentService.save(knowledgeDocument);

    }
}
