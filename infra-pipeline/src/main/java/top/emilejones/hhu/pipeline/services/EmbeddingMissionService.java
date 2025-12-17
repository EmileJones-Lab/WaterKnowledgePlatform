package top.emilejones.hhu.pipeline.services;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;

import java.util.List;

@Service
public class EmbeddingMissionService {



    public void save(@NotNull EmbeddingMission embeddingMission) {

    }


    public void delete(@NotNull String embeddingMissionId) {

    }


    @Nullable
    public EmbeddingMission findById(@NotNull String embeddingMissionId) {
        return null;
    }


    public @NotNull List<EmbeddingMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return List.of();
    }


    @NotNull
    public List<List<EmbeddingMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        return List.of();
    }


    public void saveBatch(@NotNull List<EmbeddingMission> embeddingMissionList) {

    }
}
