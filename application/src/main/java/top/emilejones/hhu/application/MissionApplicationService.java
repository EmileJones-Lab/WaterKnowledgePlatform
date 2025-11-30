package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.UUID;

@Service
public class MissionApplicationService {
    private final ApplicationEventPublisher publisher;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;

    public MissionApplicationService(
            ApplicationEventPublisher publisher,
            StructureExtractionMissionRepository structureExtractionMissionRepository,
            EmbeddingMissionRepository embeddingMissionRepository
    ) {
        this.publisher = publisher;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
    }

    public List<StructureExtractionMission> startStructureExtractionMission(List<String> sourceDocumentIdList) {
        List<StructureExtractionMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateStructureExtractionMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions;
    }

    public List<EmbeddingMission> startEmbeddingMission(List<String> sourceDocumentIdList) {
        List<EmbeddingMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateEmbeddingMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions;
    }

    private StructureExtractionMission findExistingOrCreateStructureExtractionMission(String sourceDocumentId) {
        return structureExtractionMissionRepository.selectBySourceDocumentId(sourceDocumentId).stream()
                .filter(StructureExtractionMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    StructureExtractionMission mission = StructureExtractionMission.Companion.create(
                            UUID.randomUUID().toString(),
                            sourceDocumentId
                    );
                    mission.preparedToExecution();
                    return mission;
                });
    }

    private EmbeddingMission findExistingOrCreateEmbeddingMission(String sourceDocumentId) {
        return embeddingMissionRepository.selectBySourceDocumentId(sourceDocumentId).stream()
                .filter(EmbeddingMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    EmbeddingMission mission = EmbeddingMission.Companion.create(
                            UUID.randomUUID().toString(),
                            sourceDocumentId
                    );
                    mission.preparedToExecution();
                    return mission;
                });
    }
}
