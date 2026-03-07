package top.emilejones.hhu.application.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.StructureExtractionGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.FileNodeDTO;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.TextNodeDTO;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Component
public class StructureExtractionProcessor {
    private final OcrMissionRepository ocrMissionRepository;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final ProcessedDocumentRepository processedDocumentRepository;
    private final StructureExtractionGateway structureExtractionGateway;
    private final OcrProcessor ocrProcessor;

    public StructureExtractionProcessor(OcrMissionRepository ocrMissionRepository, StructureExtractionMissionRepository structureExtractionMissionRepository, ProcessedDocumentRepository processedDocumentRepository, StructureExtractionGateway structureExtractionGateway, OcrProcessor ocrProcessor) {
        this.ocrMissionRepository = ocrMissionRepository;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.processedDocumentRepository = processedDocumentRepository;
        this.structureExtractionGateway = structureExtractionGateway;
        this.ocrProcessor = ocrProcessor;
    }

    public void process(StructureExtractionMission structureExtractionMission) {
        structureExtractionMissionRepository.save(structureExtractionMission);

        // 1. 获取并检查OCR任务状态
        List<OcrMission> ocrMissions = ocrMissionRepository.findBySourceDocumentId(structureExtractionMission.getSourceDocumentId());
        if (hasRunningOrPendingOcrMission(ocrMissions)) {
            structureExtractionMission.failure("存在运行中或等待中的OCR任务，无法开启结构提取任务");
            structureExtractionMissionRepository.save(structureExtractionMission);
            return;
        }

        // 2. 获取或执行OCR任务
        OcrMission ocrMission = getOrCreateOcrMission(structureExtractionMission.getSourceDocumentId(), ocrMissions);
        if (!ocrMission.isSuccess()) {
            structureExtractionMission.failure("OCR失败，无法开启结构提取任务");
            structureExtractionMissionRepository.save(structureExtractionMission);
            return;
        }

        // 3. 执行结构提取
        executeStructureExtraction(structureExtractionMission, ocrMission);
    }

    private boolean hasRunningOrPendingOcrMission(List<OcrMission> ocrMissions) {
        return ocrMissions.stream()
                .anyMatch(mission -> MissionStatus.RUNNING.equals(mission.getStatus()) ||
                        MissionStatus.PENDING.equals(mission.getStatus()));
    }

    private OcrMission getOrCreateOcrMission(String sourceDocumentId, List<OcrMission> ocrMissions) {
        return ocrMissions.stream()
                .filter(OcrMission::isSuccess)
                .findFirst()
                .orElseGet(() -> ocrProcessor.process(sourceDocumentId));
    }

    private void executeStructureExtraction(StructureExtractionMission structureExtractionMission, OcrMission ocrMission) {
        OcrMissionResult.Success successResult = ocrMission.getSuccessResult();
        String markdownDocumentId = Objects.requireNonNull(successResult.getMarkdownDocumentId());

        structureExtractionMission.start(markdownDocumentId);
        structureExtractionMissionRepository.save(structureExtractionMission);

        try {
            doExtract(structureExtractionMission, markdownDocumentId);
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "未知的错误";
            structureExtractionMission.failure(message);
        }

        structureExtractionMissionRepository.save(structureExtractionMission);
    }

    private void doExtract(StructureExtractionMission structureExtractionMission, String markdownDocumentId) throws Exception {
        // 获取markdown文件内容
        ProcessedDocument processedDocument = processedDocumentRepository.find(markdownDocumentId);
        if (processedDocument == null)
            throw new IllegalAccessException("OCR任务存在，但是提取出来的markdown文件却不存在");

        InputStream inputStream = processedDocumentRepository.openContent(processedDocument.getFilePath());

        // 提取markdown文件结构
        TextNodeDTO structure = structureExtractionGateway.extract(inputStream);

        // 为这个树状结构和源文件绑定
        Objects.requireNonNull(structure.getFileNode()).setFileId(structureExtractionMission.getSourceDocumentId());

        // 保存这个树状结构
        structureExtractionGateway.save(structure);

        // 记录任务状态为成功
        FileNodeDTO fileNode = structure.getFileNode();
        structureExtractionMission.success(fileNode.getId());
    }
}
