package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.List;

@Component
public class EmbeddingMissionController implements EmbeddingMissionRepository {

    private final EmbeddingMissionService embeddingMissionService;

    public EmbeddingMissionController(EmbeddingMissionService embeddingMissionService) {
        this.embeddingMissionService = embeddingMissionService;
    }

    @Override
    public void save(@NotNull EmbeddingMission embeddingMission) {
        embeddingMissionService.save(embeddingMission);
    }

    @Override
    public void delete(@NotNull String embeddingMissionId) {
        embeddingMissionService.delete(embeddingMissionId);
    }

    @Override
    @Nullable
    public EmbeddingMission findById(@NotNull String embeddingMissionId) {
        return embeddingMissionService.findById(embeddingMissionId);
    }

    @Override
    public @NotNull List<EmbeddingMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return embeddingMissionService.findBySourceDocumentId(sourceDocumentId);
    }

    @Override
    @NotNull
    public List<List<EmbeddingMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        return embeddingMissionService.findBatchBySourceDocumentId(sourceDocumentIdList);
    }

    @Override
    public void saveBatch(@NotNull List<EmbeddingMission> embeddingMissionList) {
        embeddingMissionService.saveBatch(embeddingMissionList);
    }
}
