package top.emilejones.hhu.application.platform.statemachine.actions;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import top.emilejones.hhu.application.platform.statemachine.PipelineContext;
import top.emilejones.hhu.application.platform.statemachine.PipelineEvent;
import top.emilejones.hhu.application.platform.statemachine.PipelineState;
import top.emilejones.hhu.common.FileUtils;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway;
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.repository.OcrMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.result.ProcessedDocument;
import top.emilejones.hhu.domain.result.ProcessedDocumentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OcrAction implements Action<PipelineState, PipelineEvent> {
    private final OcrMissionRepository ocrMissionRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final OcrGateway ocrGateway;
    private final ProcessedDocumentRepository processedDocumentRepository;

    public OcrAction(OcrMissionRepository ocrMissionRepository,
                     SourceDocumentRepository sourceDocumentRepository,
                     OcrGateway ocrGateway,
                     ProcessedDocumentRepository processedDocumentRepository) {
        this.ocrMissionRepository = ocrMissionRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.ocrGateway = ocrGateway;
        this.processedDocumentRepository = processedDocumentRepository;
    }

    @Override
    public void execute(StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);
        String fileId = pipelineContext.getSourceDocumentId();

        try {
            // 查找成功的 OCR 任务
            List<OcrMission> existingMissions = ocrMissionRepository.findBySourceDocumentId(fileId);
            OcrMission ocrMission = existingMissions.stream()
                    .filter(OcrMission::isSuccess)
                    .findFirst()
                    .orElseGet(() -> performOcr(fileId));

            if (ocrMission.isSuccess()) {
                pipelineContext.setOcrMission(ocrMission);
                // OCR 任务结束后不允许直接完成，必须进入结构提取阶段
                sendEvent(context, PipelineEvent.TO_STRUCTURE_EXTRACTION);
            } else {
                sendEvent(context, PipelineEvent.TO_FAILED);
            }
        } catch (Exception e) {
            sendEvent(context, PipelineEvent.TO_FAILED);
        }
    }

    private void sendEvent(StateContext<PipelineState, PipelineEvent> context, PipelineEvent event) {
        context.getStateMachine()
                .sendEvent(Mono.just(MessageBuilder.withPayload(event).build()))
                .subscribe();
    }

    private OcrMission performOcr(String sourceDocumentId) {
        OcrMission ocrMission = OcrMission.Companion.create(sourceDocumentId);
        ocrMission.start();
        ocrMissionRepository.save(ocrMission);

        Optional<SourceDocument> sourceDocumentOptional = sourceDocumentRepository.findSourceDocumentById(sourceDocumentId);
        if (sourceDocumentOptional.isEmpty()) {
            ocrMission.failure("源文件不存在！");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        SourceDocument sourceDocument = sourceDocumentOptional.get();
        byte[] pdfBytes;
        try (InputStream content = sourceDocumentRepository.openContent(sourceDocument.getFilePath())) {
            pdfBytes = content.readAllBytes();
        } catch (IOException e) {
            ocrMission.failure("读取PDF内容失败: " + e.getMessage());
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        if (!FileUtils.INSTANCE.checkPdf(pdfBytes)) {
            ocrMission.failure("不是一个OCR文件");
            ocrMissionRepository.save(ocrMission);
            return ocrMission;
        }

        Result<MinerUMarkdownFile> result = ocrGateway.minerU(pdfBytes);
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
