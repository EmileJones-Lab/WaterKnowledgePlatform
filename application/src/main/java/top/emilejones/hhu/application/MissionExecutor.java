package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.application.processor.EmbeddingProcessor;
import top.emilejones.hhu.application.processor.StructureExtractionProcessor;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

@Component
public class MissionExecutor {
    private final ApplicationEventPublisher publisher;
    private final StructureExtractionProcessor structureExtractionProcessor;
    private final EmbeddingProcessor embeddingProcessor;

    public MissionExecutor(ApplicationEventPublisher publisher, StructureExtractionProcessor structureExtractionProcessor, EmbeddingProcessor embeddingProcessor) {
        this.publisher = publisher;
        this.structureExtractionProcessor = structureExtractionProcessor;
        this.embeddingProcessor = embeddingProcessor;
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
    }
}