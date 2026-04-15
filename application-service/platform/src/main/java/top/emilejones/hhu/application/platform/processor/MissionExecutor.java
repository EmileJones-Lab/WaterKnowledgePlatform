package top.emilejones.hhu.application.platform.processor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

@Component
public class MissionExecutor {
    private final ApplicationEventPublisher publisher;
    private final StructureExtractionProcessor structureExtractionProcessor;
    private final EmbeddingProcessor embeddingProcessor;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public MissionExecutor(ApplicationEventPublisher publisher,
                           StructureExtractionProcessor structureExtractionProcessor,
                           EmbeddingProcessor embeddingProcessor,
                           SourceDocumentRepository sourceDocumentRepository,
                           KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.publisher = publisher;
        this.structureExtractionProcessor = structureExtractionProcessor;
        this.embeddingProcessor = embeddingProcessor;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    @Async
    @EventListener
    public void handlerStartStructureExtractionEvent(StructureExtractionMission mission) {
        structureExtractionProcessor.process(mission);
    }

    @Async
    @EventListener
    public void handlerStartEmbeddingMissionEvent(EmbeddingMission event) {
        embeddingProcessor.process(event);
        event.pushEvents().forEach(publisher::publishEvent);

        if (event.isSuccess()) {
            String sourceDocName = sourceDocumentRepository.findSourceDocumentById(event.getSourceDocumentId())
                    .map(it -> it.getName())
                    .orElse("Unknown File");

            KnowledgeDocument doc = KnowledgeDocument.Companion.create(
                    sourceDocName,
                    event.getId(),
                    KnowledgeDocumentType.STRUCTURE_SPLITTER
            );
            knowledgeDocumentRepository.save(doc);
        }
    }
}