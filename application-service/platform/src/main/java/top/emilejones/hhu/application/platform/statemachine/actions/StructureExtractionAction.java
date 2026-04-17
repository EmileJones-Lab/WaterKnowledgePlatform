package top.emilejones.hhu.application.platform.statemachine.actions;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import top.emilejones.hhu.application.platform.statemachine.PipelineContext;
import top.emilejones.hhu.application.platform.statemachine.PipelineEvent;
import top.emilejones.hhu.application.platform.statemachine.PipelineState;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.domain.pipeline.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.result.ProcessedDocument;

import java.io.InputStream;
import java.util.Objects;

@Component
public class StructureExtractionAction implements Action<PipelineState, PipelineEvent> {
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final ProcessedDocumentRepository processedDocumentRepository;
    private final StructureExtractionGateway structureExtractionGateway;

    public StructureExtractionAction(StructureExtractionMissionRepository structureExtractionMissionRepository,
                                     ProcessedDocumentRepository processedDocumentRepository,
                                     StructureExtractionGateway structureExtractionGateway) {
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.processedDocumentRepository = processedDocumentRepository;
        this.structureExtractionGateway = structureExtractionGateway;
    }

    @Override
    public void execute(StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);
        String fileId = pipelineContext.getSourceDocumentId();
        String topMissionId = pipelineContext.getCurrentTopMissionId();

        try {
            StructureExtractionMission mission = null;

            // 如果顶层任务本身就是结构提取任务，则直接使用它
            if (pipelineContext.getTargetState() == PipelineState.STRUCTURE_EXTRACTION) {
                mission = structureExtractionMissionRepository.find(topMissionId);
            }

            // 如果没找到或不是顶层任务，尝试寻找已成功的任务
            if (mission == null) {
                mission = structureExtractionMissionRepository.findBySourceDocumentId(fileId).stream()
                        .filter(StructureExtractionMission::isSuccess)
                        .findFirst()
                        .orElse(null);
            }

            // 如果仍然没有，则创建一个新的内部任务
            if (mission == null) {
                mission = StructureExtractionMission.Companion.create(fileId);
            }

            // 如果任务还没成功，则执行它
            if (!mission.isSuccess()) {
                performStructureExtraction(mission, pipelineContext.getOcrMission());
            }

            if (mission.isSuccess()) {
                pipelineContext.setStructureExtractionMission(mission);
                // 决定下一步
                if (pipelineContext.getTargetState() == PipelineState.EMBEDDING || pipelineContext.getTargetState() == PipelineState.SUMMARY) {
                    sendEvent(context, PipelineEvent.TO_SUMMARY);
                } else {
                    sendEvent(context, PipelineEvent.TO_COMPLETED);
                }
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

    private void performStructureExtraction(StructureExtractionMission structureExtractionMission, OcrMission ocrMission) {
        if (ocrMission == null || !ocrMission.isSuccess()) {
            structureExtractionMission.failure("OCR任务未完成或失败，无法进行结构提取");
            structureExtractionMissionRepository.save(structureExtractionMission);
            return;
        }

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
