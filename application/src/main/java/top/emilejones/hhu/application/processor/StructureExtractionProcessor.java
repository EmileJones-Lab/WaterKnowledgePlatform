package top.emilejones.hhu.application.processor;

import org.springframework.stereotype.Component;
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
import java.util.Optional;

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

    public StructureExtractionMission process(StructureExtractionMission structureExtractionMission) {
        structureExtractionMissionRepository.save(structureExtractionMission);
        // 获取曾经执行过的OCR任务，如果曾经没有执行成功的OCR任务，那么就执行OCR任务
        List<OcrMission> ocrMissions = ocrMissionRepository.findBySourceDocumentId(structureExtractionMission.getSourceDocumentId());
        Optional<OcrMission> succeesfulOcrMissionOptional = ocrMissions.stream().filter(OcrMission::isSuccess).findFirst();
        // todo: 如果有运行中和等待中的OCR任务，应该开启结构化提取任务失败
        OcrMission ocrMission = succeesfulOcrMissionOptional.orElseGet(() -> ocrProcessor.process(structureExtractionMission.getSourceDocumentId()));
        if (!ocrMission.isSuccess()) {
            structureExtractionMission.failure("OCR失败，无法开启结构提取任务");
            structureExtractionMissionRepository.save(structureExtractionMission);
            return structureExtractionMission;
        }
        // 开始根据之前的OCR结果进行结构提取
        OcrMissionResult.Success successResult = ocrMission.getSuccessResult();
        String markdownDocumentId =
                Objects.requireNonNull(successResult.getMarkdownDocumentId());
        structureExtractionMission.start(markdownDocumentId);
        structureExtractionMissionRepository.save(structureExtractionMission);
        try {
            // 获取markdown文件内容
            Optional<ProcessedDocument> processedDocumentOptional = processedDocumentRepository.findById(markdownDocumentId);
            if (processedDocumentOptional.isEmpty())
                throw new IllegalAccessException("OCR任务存在，但是提取出来的markdown文件却不存在");
            InputStream inputStream = processedDocumentRepository.openContent(processedDocumentOptional.get().getFilePath());
            // 提取markdown文件结构
            TextNodeDTO structure = structureExtractionGateway.extract(inputStream);
            // 为这个树状结构和源文件绑定
            Objects.requireNonNull(structure.getFileNode()).setFileId(structureExtractionMission.getSourceDocumentId());
            // 保存这个树状结构
            structureExtractionGateway.save(structure);
            // 记录任务状态为成功
            FileNodeDTO fileNode = structure.getFileNode();
            structureExtractionMission.success(fileNode.getId());
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "未知的错误";
            structureExtractionMission.failure(message);
        }
        structureExtractionMissionRepository.save(structureExtractionMission);
        return structureExtractionMission;
    }
}
