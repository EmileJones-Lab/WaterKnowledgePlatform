package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.List;

/**
 * 向量化任务仓库实现，衔接领域仓储与服务逻辑。
 * 它实现了EmbeddingMissionRepository接口，通过调用EmbeddingMissionService来完成具体的业务处理。
 *
 * @author Yeyezhi
 */
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
    public EmbeddingMission find(@NotNull String embeddingMissionId) {
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
    public void saveBatch(@NotNull List<? extends EmbeddingMission> valueList) {
        embeddingMissionService.saveBatch(valueList);
    }

    @Override
    public void deleteBatch(@NotNull List<? extends String> keyList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    @NotNull
    public List<EmbeddingMission> findBatch(@NotNull List<? extends String> keyList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
