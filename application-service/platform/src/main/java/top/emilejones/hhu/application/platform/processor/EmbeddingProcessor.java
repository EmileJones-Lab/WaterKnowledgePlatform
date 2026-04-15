package top.emilejones.hhu.application.platform.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;
import top.emilejones.hhu.domain.result.MissionStatus;

import java.util.List;
import java.util.Objects;

@Component
public class EmbeddingProcessor {
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final EmbeddingGateway embeddingGateway;
    private final StructureExtractionProcessor structureExtractionProcessor;

    public EmbeddingProcessor(StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingMissionRepository embeddingMissionRepository, EmbeddingGateway embeddingGateway, StructureExtractionProcessor structureExtractionProcessor) {
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.structureExtractionProcessor = structureExtractionProcessor;
    }

    public void process(EmbeddingMission embeddingMission) {
        embeddingMissionRepository.save(embeddingMission);

        // 1. 获取并检查结构提取任务状态
        List<StructureExtractionMission> structureExtractionMissions = structureExtractionMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());
        if (hasRunningOrPendingStructureExtraction(structureExtractionMissions)) {
            embeddingMission.failure("存在运行中或等待中的结构提取任务，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return;
        }

        // 2. 获取或执行结构提取任务
        StructureExtractionMission structureExtractionMission = getOrCreateStructureExtractionMission(embeddingMission.getSourceDocumentId(), structureExtractionMissions);
        if (!structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return;
        }

        // 3. 执行向量化
        executeEmbedding(embeddingMission, structureExtractionMission);
    }

    private boolean hasRunningOrPendingStructureExtraction(List<StructureExtractionMission> missions) {
        return missions.stream()
                .anyMatch(mission -> MissionStatus.RUNNING.equals(mission.getStatus()) ||
                        MissionStatus.PENDING.equals(mission.getStatus()));
    }

    private StructureExtractionMission getOrCreateStructureExtractionMission(String sourceDocumentId, List<StructureExtractionMission> missions) {
        return missions.stream()
                .filter(StructureExtractionMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    StructureExtractionMission newMission = StructureExtractionMission.Companion.create(sourceDocumentId);
                    structureExtractionProcessor.process(newMission);
                    return newMission;
                });
    }

    private void executeEmbedding(EmbeddingMission embeddingMission, StructureExtractionMission structureExtractionMission) {
        StructureExtractionMissionResult.Success successResult = structureExtractionMission.getSuccessResult();
        String fileNodeId = successResult.getFileNodeId();
        embeddingMission.start(fileNodeId);

        Result<String> result = embeddingGateway.embed(fileNodeId);

        if (result.isSuccess()) {
            embeddingMission.success(result.getOrThrow());
        } else {
            Throwable exception = result.exceptionOrNull();
            String msg = (exception != null && exception.getMessage() != null) ? exception.getMessage() : "向量化过程发生未知异常";
            embeddingMission.failure(msg);
        }

        embeddingMissionRepository.save(embeddingMission);
    }

    /**
     * @deprecated 请使用 embeddingGateway.embed(fileNodeId)
     */
    @Deprecated
    private void doEmbed(EmbeddingMission embeddingMission, String fileNodeId) throws Exception {
        Result<String> result = embeddingGateway.embed(fileNodeId);
        if (result.isFailure()) {
            Throwable ex = Objects.requireNonNull(result.exceptionOrNull());
            embeddingMission.failure(ex.getMessage());
        }
        embeddingMission.success(result.getOrThrow());
    }
}
