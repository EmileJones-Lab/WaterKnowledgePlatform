package top.emilejones.hhu.pipeline.controller;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.pipeline.services.OcrMissionService;


import java.util.List;

/**
 * OCR任务仓库实现，负责OCR任务的数据操作和业务逻辑的协调。
 * 它实现了OcrMissionRepository接口，通过调用OcrMissionService来完成具体的业务处理。
 * @author Yeyezhi
 */
@Component
public class OcrMissionController implements OcrMissionRepository {

    private final OcrMissionService ocrMissionService;

    public OcrMissionController(OcrMissionService ocrMissionService) {
        this.ocrMissionService = ocrMissionService;
    }

    @Override
    public void save(@NotNull OcrMission ocrMission) {
        ocrMissionService.save(ocrMission);
    }

    @Override
    public void saveBatch(@NotNull List<OcrMission> ocrMissionList) {
        ocrMissionService.saveBatch(ocrMissionList);
    }

    @Override
    @NotNull
    public List<String> findStartOcrMissionSourceDocumentIdByCreateTimeDesc(int limit, int offset) {
        return ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit, offset);
    }

    @Override
    @NotNull
    public List<OcrMission> findBySourceDocumentId(@NotNull String sourceDocumentId) {
        return ocrMissionService.findBySourceDocumentId(sourceDocumentId);
    }

    @Override
    @NotNull
    public List<List<OcrMission>> findBatchBySourceDocumentId(@NotNull List<String> sourceDocumentIdList) {
        return ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIdList);
    }

    @Override
    public void delete(@NotNull String ocrMissionId) {
        ocrMissionService.delete(ocrMissionId);
    }

    @Override
    @Nullable
    public OcrMission findById(@NotNull String ocrMissionId) {
        return ocrMissionService.findById(ocrMissionId);
    }
}
