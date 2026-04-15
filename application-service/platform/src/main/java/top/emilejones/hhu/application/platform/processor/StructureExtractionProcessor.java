package top.emilejones.hhu.application.platform.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.domain.pipeline.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.result.MissionStatus;
import top.emilejones.hhu.domain.result.ProcessedDocument;

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

        // 1. 获取markdown文件内容
        ProcessedDocument processedDocument = processedDocumentRepository.find(markdownDocumentId);
        if (processedDocument == null) {
            structureExtractionMission.failure("OCR任务存在，但是提取出来的markdown文件却不存在");
            structureExtractionMissionRepository.save(structureExtractionMission);
            return;
        }

        InputStream inputStream = processedDocumentRepository.openContent(processedDocument.getFilePath());
        
        // 2. 提取markdown文件结构
        Result<String> result = structureExtractionGateway.extract(inputStream, structureExtractionMission.getSourceDocumentId());
        
        if (result.isFailure()) {
            Throwable ex = result.exceptionOrNull();
            String message = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "结构提取过程发生未知错误";
            structureExtractionMission.failure(message);
        } else {
            // 3. 记录任务状态为成功
            structureExtractionMission.success(result.getOrThrow());
        }

        structureExtractionMissionRepository.save(structureExtractionMission);
    }
}
