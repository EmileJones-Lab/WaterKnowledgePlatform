package top.emilejones.hhu.pipeline.services;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;

@Service
public class StructureExtractionMissionService {


    public void save(@NotNull StructureExtractionMission structureExtractionMission) {

    }


    public void saveBatch(@NotNull List<StructureExtractionMission> structureExtractionMissionList) {

    }

    @NotNull
    public List<StructureExtractionMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return List.of();
    }


    @NotNull
    public List<List<StructureExtractionMission>> findBySourceDocumentIdList(@NotNull List<String> sourceDocumentIdList) {
        return List.of();
    }


    public void delete(@NotNull String structureExtractionMissionId) {

    }

    @Nullable
    public StructureExtractionMission findById(@NotNull String structureExtractionMissionId) {
        return null;
    }
}
