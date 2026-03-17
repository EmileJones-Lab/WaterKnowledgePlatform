package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.List;

/**
 * 结构化抽取任务仓库实现，衔接领域仓储与服务逻辑。
 * 它实现了StructureExtractionMissionRepository接口，通过调用StructureExtractionMissionService来完成具体的业务处理。
 *
 * @author Yeyezhi
 */
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
    public void saveBatch(@NotNull List<? extends StructureExtractionMission> structureExtractionMissionList) {
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
    public void deleteBatch(@NotNull List<? extends String> keyList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Nullable
    public StructureExtractionMission find(@NotNull String structureExtractionMissionId) {
        return structureExtractionMissionService.findById(structureExtractionMissionId);
    }

    @Override
    @NotNull
    public List<StructureExtractionMission> findBatch(@NotNull List<? extends String> keyList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
