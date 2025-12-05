package top.emilejones.hhu.knowledge.KnowledgeDocumentTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.knowledge.TestApplication;
import top.emilejones.hhu.knowledge.controller.KnowledgeDocumentController;
import top.emilejones.hhu.knowledge.pojo.dto.KnowledgeDocumentDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Autowired
    private KnowledgeDocumentController knowledgeDocumentController;

    @Test
    public void saveTest(){
        String name = "test";
        String embeddingMissionId = String.valueOf(UUID.randomUUID());
        KnowledgeDocumentType type = KnowledgeDocumentType.STRUCTURE_SPLITTER;
        Instant createTime = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        KnowledgeDocument knowledgeDocument = new KnowledgeDocument("684f71bb-278f-4675-aa17-a14e6274b735", name, embeddingMissionId, type, createTime);
        knowledgeDocumentController.save(knowledgeDocument);
    }
}
