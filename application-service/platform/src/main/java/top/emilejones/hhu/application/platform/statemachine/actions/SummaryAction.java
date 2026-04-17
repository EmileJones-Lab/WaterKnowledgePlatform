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

import java.util.Objects;

@Component
public class SummaryAction implements Action<PipelineState, PipelineEvent> {
    private final StructureExtractionGateway structureExtractionGateway;

    public SummaryAction(StructureExtractionGateway structureExtractionGateway) {
        this.structureExtractionGateway = structureExtractionGateway;
    }

    @Override
    public void execute(StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);
        String sourceDocumentId = pipelineContext.getSourceDocumentId();

        try {
            Result<String> result = structureExtractionGateway.summary(sourceDocumentId);
            if (result.isSuccess()) {
                sendEvent(context, PipelineEvent.TO_EMBEDDING);
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
}
