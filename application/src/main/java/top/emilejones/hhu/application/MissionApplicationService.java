package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.utils.DtoConverter;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MissionApplicationService {
    private final ApplicationEventPublisher publisher;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;

    /**
     * 构造函数。
     *
     * @param publisher                            Spring事件发布器。
     * @param structureExtractionMissionRepository 结构提取任务仓储。
     * @param embeddingMissionRepository           向量化任务仓储。
     */
    public MissionApplicationService(
            ApplicationEventPublisher publisher,
            StructureExtractionMissionRepository structureExtractionMissionRepository,
            EmbeddingMissionRepository embeddingMissionRepository
    ) {
        this.publisher = publisher;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
    }

    /**
     * 删除指定的结构提取任务。
     *
     * @param documentSplittingMissionId 结构提取任务的唯一标识。
     */
    public void deleteExtractStructureMission(String documentSplittingMissionId) {

    }

    /**
     * 批量开启结构提取任务。
     * 如果之前没有开启过OCR任务，则自动开启一个OCR任务。
     *
     * @param sourceDocumentIdList 文件的唯一ID列表。
     * @return 这批结构提取任务的详细信息列表。
     */
    public List<DocumentSplittingMissionDTO> startStructureExtractionMission(List<String> sourceDocumentIdList) {
        List<StructureExtractionMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateStructureExtractionMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions.stream()
                .map(DtoConverter::toDocumentSplittingMissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量开启层次结构向量化任务。
     * 如果此文件没有开启过OCR任务和结构提取任务，此接口会自动按顺序开启上述任务。
     *
     * @param sourceDocumentIdList 文件的唯一ID列表。
     * @return 这批向量化任务的详细信息列表。
     */
    public List<EmbeddingMissionDTO> startEmbeddingMission(List<String> sourceDocumentIdList) {
        List<EmbeddingMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateEmbeddingMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions.stream()
                .map(DtoConverter::toEmbeddingMissionDTO)
                .collect(Collectors.toList());
    }

    private StructureExtractionMission findExistingOrCreateStructureExtractionMission(String sourceDocumentId) {
        return structureExtractionMissionRepository.findBySourceDocumentId(sourceDocumentId).stream()
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
        return embeddingMissionRepository.findBySourceDocumentId(sourceDocumentId).stream()
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

