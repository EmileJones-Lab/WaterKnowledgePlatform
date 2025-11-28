package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.event.StartEmbeddingMissionEvent;
import top.emilejones.hhu.application.event.StartStructureExtractionMissionEvent;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.UUID;

@Service
public class MissionApplicationService {
    private final ApplicationEventPublisher publisher;

    public MissionApplicationService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public List<StructureExtractionMission> startStructureExtractionMission(List<String> sourceDocumentIdList) {
        List<StructureExtractionMission> structureExtractionMissionList = sourceDocumentIdList.stream()
                .map(sourceDocumentId -> StructureExtractionMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId))
                .toList();
        structureExtractionMissionList.stream()
                .map(StartStructureExtractionMissionEvent::new)
                .forEach(publisher::publishEvent);
        return structureExtractionMissionList;
    }

    public List<EmbeddingMission> startEmbeddingMission(List<String> sourceDocumentIdList) {
        List<EmbeddingMission> structureExtractionMissionList = sourceDocumentIdList.stream()
                .map(sourceDocumentId -> EmbeddingMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId))
                .toList();
        structureExtractionMissionList.stream()
                .map(StartEmbeddingMissionEvent::new)
                .forEach(publisher::publishEvent);
        return structureExtractionMissionList;
    }
}
