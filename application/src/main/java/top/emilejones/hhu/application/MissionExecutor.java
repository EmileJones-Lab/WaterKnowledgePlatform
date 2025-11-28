package top.emilejones.hhu.application;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.application.event.StartEmbeddingMissionEvent;
import top.emilejones.hhu.application.event.StartStructureExtractionMissionEvent;
import top.emilejones.hhu.domain.pipeline.FileNode;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.OcrGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.SourceDocumentGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.StructureExtractionGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.SourceDocument;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class MissionExecutor {

    private final StructureExtractionGateway structureExtractionGateway;
    private final OcrMissionRepository ocrMissionRepository;
    private final SourceDocumentGateway sourceDocumentGateway;
    private final OcrGateway ocrGateway;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingGateway embeddingGateway;
    private final EmbeddingMissionRepository embeddingMissionRepository;

    public MissionExecutor(StructureExtractionGateway structureExtractionGateway, OcrMissionRepository ocrMissionRepository, SourceDocumentGateway sourceDocumentGateway, OcrGateway ocrGateway, StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingGateway embeddingGateway, EmbeddingMissionRepository embeddingMissionRepository) {
        this.structureExtractionGateway = structureExtractionGateway;
        this.ocrMissionRepository = ocrMissionRepository;
        this.sourceDocumentGateway = sourceDocumentGateway;
        this.ocrGateway = ocrGateway;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.embeddingMissionRepository = embeddingMissionRepository;
    }

    @Async("missionEventExecutor")
    @EventListener
    public void handlerStartStructureExtractionEvent(StartStructureExtractionMissionEvent event) {
        StructureExtractionMission structureExtractionMission = event.getStructureExtractionMission();
        startStructureExtraction(structureExtractionMission);
    }

    @Async("missionEventExecutor")
    @EventListener
    public void handlerStartEmbeddingMissionEvent(StartEmbeddingMissionEvent event) {
        EmbeddingMission embeddingMission = event.getEmbeddingMission();
        startEmbeddingMission(embeddingMission);
    }

    private OcrMission startOcrMission(String sourceDocumentId) {
        OcrMission ocrMission = OcrMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);
        ocrMission.start();
        ocrMissionRepository.save(ocrMission);
        SourceDocument sourceDocument = sourceDocumentGateway.getSourceDocument(ocrMission.getSourceDocumentId());
        if (sourceDocument == null) {
            ocrMission.failure("源文件不存在！");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }
        try {
            ProcessedDocument processedDocument = ocrGateway.startOcr(sourceDocument.getInputStream());
            ocrMission.success(processedDocument);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            ocrMission.failure(msg);
        }

        ocrMissionRepository.save(ocrMission);
        return ocrMission;
    }

    private StructureExtractionMission startStructureExtraction(StructureExtractionMission structureExtractionMission) {
        // 获取曾经执行过的OCR任务，如果曾经没有执行成功的OCR任务，那么就执行OCR任务
        List<OcrMission> ocrMissions = ocrMissionRepository.selectBySourceDocumentId(structureExtractionMission.getSourceDocumentId());
        Optional<OcrMission> succeesfulOcrMissionOptional = ocrMissions.stream().filter(OcrMission::isSuccess).findFirst();
        OcrMission ocrMission = succeesfulOcrMissionOptional.orElseGet(() -> startOcrMission(structureExtractionMission.getSourceDocumentId()));
        if (!ocrMission.isSuccess()) {
            structureExtractionMission.failure("OCR失败，无法开启结构提取任务");
            ocrMissionRepository.save(ocrMission);
            return structureExtractionMission;
        }
        // 开始根据之前的OCR结果进行结构提取
        OcrMissionResult.Success successResult = ocrMission.getSuccessResult();
        ProcessedDocument processedDocument = successResult.getProcessedDocument();
        structureExtractionMission.start(processedDocument.getId());
        structureExtractionMissionRepository.save(structureExtractionMission);
        try {
            String processedDocumentId =
                    Objects.requireNonNull(structureExtractionMission.getProcessedDocumentId());
            FileNode fileNode = structureExtractionGateway.extract(processedDocumentId);
            structureExtractionMission.success(fileNode.getElementId(), fileNode.getChildNodeNumber());
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "未知的错误";
            structureExtractionMission.failure(message);
        }
        structureExtractionMissionRepository.save(structureExtractionMission);
        return structureExtractionMission;
    }

    private EmbeddingMission startEmbeddingMission(EmbeddingMission embeddingMission) {
        // 获取曾经执行过的结构提取任务，如果没有执行成功的结构提取任务，则开启一个新的结构提取任务。
        List<StructureExtractionMission> structureExtractionMissions = structureExtractionMissionRepository.selectBySourceDocumentId(embeddingMission.getSourceDocumentId());
        Optional<StructureExtractionMission> succeesfulStructureExtractionOptional = structureExtractionMissions.stream().filter(StructureExtractionMission::isSuccess).findFirst();
        StructureExtractionMission structureExtractionMission = succeesfulStructureExtractionOptional.orElseGet(() ->
                startStructureExtraction(StructureExtractionMission.Companion.create(UUID.randomUUID().toString(), embeddingMission.getSourceDocumentId()))
        );
        if (!structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return embeddingMission;
        }

        // 根据之前的结构提取结果进行向量化任务
        StructureExtractionMissionResult.Success successResult = structureExtractionMission.getSuccessResult();
        embeddingMission.start(successResult.getFileNodeId());
        embeddingMissionRepository.save(embeddingMission);
        try {
            embeddingGateway.embed(Objects.requireNonNull(embeddingMission.getFileNodeId()));
            embeddingMission.success();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            embeddingMission.failure(msg);
        }
        embeddingMissionRepository.save(embeddingMission);
        return embeddingMission;
    }
}
