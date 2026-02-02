package top.emilejones.hhu.application.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.common.utils.FileUtils;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.infrastruction.SourceDocumentRepository;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.OcrGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto.MinerUMarkdownFile;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class OcrProcessor {
    private final OcrMissionRepository ocrMissionRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final OcrGateway ocrGateway;
    private final ProcessedDocumentRepository processedDocumentRepository;

    public OcrProcessor(OcrMissionRepository ocrMissionRepository, SourceDocumentRepository sourceDocumentRepository, OcrGateway ocrGateway, ProcessedDocumentRepository processedDocumentRepository) {
        this.ocrMissionRepository = ocrMissionRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.ocrGateway = ocrGateway;
        this.processedDocumentRepository = processedDocumentRepository;
    }

    public OcrMission process(String sourceDocumentId) {
        OcrMission ocrMission = OcrMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);
        ocrMission.start();
        ocrMissionRepository.save(ocrMission);
        Optional<SourceDocument> sourceDocumentOptional = sourceDocumentRepository.findSourceDocumentById(ocrMission.getSourceDocumentId());
        if (sourceDocumentOptional.isEmpty()) {
            ocrMission.failure("源文件不存在！");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        try {
            SourceDocument sourceDocument = sourceDocumentOptional.get();
            InputStream content = sourceDocumentRepository.openContent(sourceDocument.getFilePath());
            boolean isPdf = FileUtils.INSTANCE.isPdf(content);
            if (!isPdf)
                throw new IllegalAccessException("不是一个OCR文件");
            MinerUMarkdownFile minerUMarkdownFile = ocrGateway.minerU(content);
            String markdownProcessedFileId = UUID.randomUUID().toString();
            ProcessedDocument markdownDocument = ProcessedDocument.Companion.create(markdownProcessedFileId, sourceDocument.getId(), generateFilePath(markdownProcessedFileId + ".md"), ProcessedDocumentType.MARKDOWN);
            minerUMarkdownFile.getImages()
                    .forEach(minerUImage -> {
                        ProcessedDocument imageDocument = ProcessedDocument.Companion.create(UUID.randomUUID().toString(), sourceDocument.getId(), generateFilePath(minerUImage.getRelativePath()), ProcessedDocumentType.IMAGE);
                        processedDocumentRepository.save(imageDocument, new ByteArrayInputStream(minerUImage.getData()));
                    });
            processedDocumentRepository.save(markdownDocument, new ByteArrayInputStream(minerUMarkdownFile.getMarkdownContent().getBytes(StandardCharsets.UTF_8)));

            ocrMission.success(markdownDocument.getId());
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            ocrMission.failure(msg);
        }

        ocrMissionRepository.save(ocrMission);
        return ocrMission;
    }

    private String generateFilePath(String relativePath) {
        LocalDate now = LocalDate.now();
        return String.format("/bamboo/StructureExtraction/%d/%d/%d/%s",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                relativePath);
    }
}