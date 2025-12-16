package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;

@Component
public class StructureExtractionMissionController implements StructureExtractionMissionRepository {

    @Override
    public void save(@NotNull StructureExtractionMission structureExtractionMission) {

    }

    @Override
    public void saveBatch(@NotNull List<StructureExtractionMission> structureExtractionMissionList) {

    }

    @Override
    @NotNull
    public List<StructureExtractionMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return List.of();
    }

    @Override
    @NotNull
    public List<List<StructureExtractionMission>> findBySourceDocumentIdList(@NotNull List<String> sourceDocumentIdList) {
        return List.of();
    }

    @Override
    public void delete(@NotNull String structureExtractionMissionId) {

    }

    @Override
    @Nullable
    public StructureExtractionMission findById(@NotNull String structureExtractionMissionId) {
        return null;
    }
}
