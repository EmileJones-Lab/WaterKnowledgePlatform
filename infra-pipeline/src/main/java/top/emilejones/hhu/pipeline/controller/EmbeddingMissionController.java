package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;

import java.util.List;

@Component
public class EmbeddingMissionController implements EmbeddingMissionRepository {

    @Override
    public void save(@NotNull EmbeddingMission embeddingMission) {

    }

    @Override
    public void delete(@NotNull String embeddingMissionId) {

    }

    @Override
    @Nullable
    public EmbeddingMission findById(@NotNull String embeddingMissionId) {
        return null;
    }

    @Override
    public @NotNull List<EmbeddingMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return List.of();
    }

    @Override
    @NotNull
    public List<List<EmbeddingMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        return List.of();
    }

    @Override
    public void saveBatch(@NotNull List<EmbeddingMission> embeddingMissionList) {

    }
}
