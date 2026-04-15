package top.emilejones.hhu.application.platform.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.common.FileUtils;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.result.ProcessedDocument;
import top.emilejones.hhu.domain.result.ProcessedDocumentType;
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway;
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile;
import top.emilejones.hhu.domain.pipeline.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
        OcrMission ocrMission = OcrMission.Companion.create(sourceDocumentId);
        ocrMission.start();
        ocrMissionRepository.save(ocrMission);
        Optional<SourceDocument> sourceDocumentOptional = sourceDocumentRepository.findSourceDocumentById(ocrMission.getSourceDocumentId());
        if (sourceDocumentOptional.isEmpty()) {
            ocrMission.failure("源文件不存在！");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        SourceDocument sourceDocument = sourceDocumentOptional.get();
        BufferedInputStream content = FileUtils.INSTANCE.checkPdf(sourceDocumentRepository.openContent(sourceDocument.getFilePath()));
        if (content == null) {
            ocrMission.failure("不是一个OCR文件");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        Result<MinerUMarkdownFile> result = ocrGateway.minerU(content);
        if (result.isFailure()) {
            Throwable ex = result.exceptionOrNull();
            String msg = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "OCR识别失败";
            ocrMission.failure(msg);
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        MinerUMarkdownFile minerUMarkdownFile = result.getOrThrow();
        // 保存文件，markdown文件名称随机生成
        ProcessedDocument markdownDocument = ProcessedDocument.Companion.create(sourceDocument.getId(), generateFilePath(UUID.randomUUID() + ".md"), ProcessedDocumentType.MARKDOWN);
        minerUMarkdownFile.getImages()
                .forEach(minerUImage -> {
                    ProcessedDocument imageDocument = ProcessedDocument.Companion.create(sourceDocument.getId(), generateFilePath(minerUImage.getRelativePath()), ProcessedDocumentType.IMAGE);
                    processedDocumentRepository.save(imageDocument, new ByteArrayInputStream(minerUImage.getData()));
                });
        processedDocumentRepository.save(markdownDocument, new ByteArrayInputStream(minerUMarkdownFile.getMarkdownContent().getBytes(StandardCharsets.UTF_8)));

        ocrMission.success(markdownDocument.getId());
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