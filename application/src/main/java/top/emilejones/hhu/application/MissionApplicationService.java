package top.emilejones.hhu.application;

import org.springframework.stereotype.Service;
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

@Service
public class MissionApplicationService {

    private final StructureExtractionGateway structureExtractionGateway;
    private final OcrMissionRepository ocrMissionRepository;
    private final SourceDocumentGateway sourceDocumentGateway;
    private final OcrGateway ocrGateway;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingGateway embeddingGateway;
    private final EmbeddingMissionRepository embeddingMissionRepository;

    public MissionApplicationService(StructureExtractionGateway structureExtractionGateway, OcrMissionRepository ocrMissionRepository, SourceDocumentGateway sourceDocumentGateway, OcrGateway ocrGateway, StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingGateway embeddingGateway, EmbeddingMissionRepository embeddingMissionRepository) {
        this.structureExtractionGateway = structureExtractionGateway;
        this.ocrMissionRepository = ocrMissionRepository;
        this.sourceDocumentGateway = sourceDocumentGateway;
        this.ocrGateway = ocrGateway;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.embeddingMissionRepository = embeddingMissionRepository;
    }

    public StructureExtractionMission startStructureExtraction(String sourceDocumentId) {
        List<OcrMission> ocrMissions = ocrMissionRepository.selectBySourceDocumentId(sourceDocumentId);
        Optional<OcrMission> first = ocrMissions.stream().filter(OcrMission::isSuccess).findFirst();
        StructureExtractionMission structureExtractionMission = StructureExtractionMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);

        if (first.isEmpty() || !first.get().isSuccess()) {
            structureExtractionMission.failure("OCR任务没有成功，无法开启文本结构提取任务");
            return structureExtractionMission;
        }

        OcrMission ocrMission = first.get();
        OcrMissionResult.Success successResult = ocrMission.getSuccessResult();

        ProcessedDocument processedDocument = successResult.getProcessedDocument();
        structureExtractionMission.setProcessedDocumentId(processedDocument.getId());

        runStructureExtractionMission(structureExtractionMission);

        return structureExtractionMission;
    }

    public EmbeddingMission startEmbeddingMission(String sourceDocumentId) {
        List<StructureExtractionMission> structureExtractionMissions = structureExtractionMissionRepository.selectBySourceDocumentId(sourceDocumentId);
        Optional<StructureExtractionMission> first = structureExtractionMissions.stream().filter(StructureExtractionMission::isSuccess).findFirst();
        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);

        if (first.isEmpty() || !first.get().isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务");
            return embeddingMission;
        }

        StructureExtractionMission extractionMission = first.get();
        StructureExtractionMissionResult.Success successResult = extractionMission.getSuccessResult();

        embeddingMission.setFileNodeId(successResult.getFileNodeId());

        runEmbeddingMission(embeddingMission);

        embeddingMissionRepository.save(embeddingMission);
        return embeddingMission;
    }

    public OcrMission startOcrMission(String sourceDocumentId) {
        OcrMission ocrMission = OcrMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);
        runOcrMission(ocrMission);
        return ocrMission;
    }

    private void runEmbeddingMission(EmbeddingMission embeddingMission) {
        embeddingMission.start();
        try {
            embeddingGateway.embed(Objects.requireNonNull(embeddingMission.getFileNodeId()));
            embeddingMission.success();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            embeddingMission.failure(msg);
        }
        embeddingMissionRepository.save(embeddingMission);
    }

    private void runOcrMission(OcrMission ocrMission) {
        ocrMission.start();
        SourceDocument sourceDocument = sourceDocumentGateway.getSourceDocument(ocrMission.getSourceDocumentId());
        if (sourceDocument == null) {
            ocrMission.failure("源文件不存在！");
            return;
        }

        try {
            ProcessedDocument processedDocument = ocrGateway.startOcr(sourceDocument.getInputStream());
            ocrMission.success(processedDocument);
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            ocrMission.failure(msg);
        }

        ocrMissionRepository.save(ocrMission);
    }

    private void runStructureExtractionMission(StructureExtractionMission structureExtractionMission) {
        structureExtractionMission.start();

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
    }
}
