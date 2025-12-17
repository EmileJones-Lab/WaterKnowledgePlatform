package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.List;

@Component
public class StructureExtractionMissionController implements StructureExtractionMissionRepository {

    private final StructureExtractionMissionService structureExtractionMissionService;

    public StructureExtractionMissionController(StructureExtractionMissionService structureExtractionMissionService) {
        this.structureExtractionMissionService = structureExtractionMissionService;
    }

    @Override
    public void save(@NotNull StructureExtractionMission structureExtractionMission) {
        structureExtractionMissionService.save(structureExtractionMission);
    }

    @Override
    public void saveBatch(@NotNull List<StructureExtractionMission> structureExtractionMissionList) {
        structureExtractionMissionService.saveBatch(structureExtractionMissionList);
    }

    @Override
    @NotNull
    public List<StructureExtractionMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return structureExtractionMissionService.findBySourceDocumentId(sourceDocumentId);
    }

    @Override
    @NotNull
    public List<List<StructureExtractionMission>> findBySourceDocumentIdList(@NotNull List<String> sourceDocumentIdList) {
        return structureExtractionMissionService.findBySourceDocumentIdList(sourceDocumentIdList);
    }

    @Override
    public void delete(@NotNull String structureExtractionMissionId) {
        structureExtractionMissionService.delete(structureExtractionMissionId);
    }

    @Override
    @Nullable
    public StructureExtractionMission findById(@NotNull String structureExtractionMissionId) {
        return structureExtractionMissionService.findById(structureExtractionMissionId);
    }
}
