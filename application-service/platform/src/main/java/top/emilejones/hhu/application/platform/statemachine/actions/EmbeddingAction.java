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
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;

@Component
public class EmbeddingAction implements Action<PipelineState, PipelineEvent> {
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final EmbeddingGateway embeddingGateway;

    public EmbeddingAction(EmbeddingMissionRepository embeddingMissionRepository,
                           EmbeddingGateway embeddingGateway) {
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.embeddingGateway = embeddingGateway;
    }

    @Override
    public void execute(StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);
        String fileId = pipelineContext.getSourceDocumentId();
        String topMissionId = pipelineContext.getCurrentTopMissionId();

        try {
            EmbeddingMission mission = null;

            // 如果顶层任务本身就是向量化任务，则直接使用它
            if (pipelineContext.getTargetState() == PipelineState.EMBEDDING) {
                mission = embeddingMissionRepository.find(topMissionId);
            }

            // 如果没找到或不是顶层任务，尝试寻找已成功的任务
            if (mission == null) {
                mission = embeddingMissionRepository.findBySourceDocumentId(fileId).stream()
                        .filter(EmbeddingMission::isSuccess)
                        .findFirst()
                        .orElse(null);
            }

            // 如果仍然没有，则创建一个新的内部任务
            if (mission == null) {
                mission = EmbeddingMission.Companion.create(fileId);
            }

            // 如果任务还没成功，则执行它
            if (!mission.isSuccess()) {
                performEmbedding(mission, pipelineContext.getStructureExtractionMission());
            }

            if (mission.isSuccess()) {
                pipelineContext.setEmbeddingMission(mission);
                sendEvent(context, PipelineEvent.TO_COMPLETED);
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

    private void performEmbedding(EmbeddingMission embeddingMission, StructureExtractionMission structureExtractionMission) {
        if (structureExtractionMission == null || !structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未完成或失败，无法进行向量化");
            embeddingMissionRepository.save(embeddingMission);
            return;
        }

        StructureExtractionMissionResult.Success successResult = structureExtractionMission.getSuccessResult();
        String fileNodeId = successResult.getFileNodeId();
        embeddingMission.start(fileNodeId);
        embeddingMissionRepository.save(embeddingMission);

        Result<String> result = embeddingGateway.embed(fileNodeId);

        if (result.isSuccess()) {
            embeddingMission.success(result.getOrThrow());
        } else {
            Throwable exception = result.exceptionOrNull();
            String msg = (exception != null && exception.getMessage() != null) ? exception.getMessage() : "向量化过程发生未知异常";
            embeddingMission.failure(msg);
        }

        embeddingMissionRepository.save(embeddingMission);
    }
}
