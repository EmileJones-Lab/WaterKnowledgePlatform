package top.emilejones.hhu.application.platform.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import top.emilejones.hhu.application.platform.statemachine.actions.*;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class PipelineStateMachineConfig extends EnumStateMachineConfigurerAdapter<PipelineState, PipelineEvent> {

    private final OcrAction ocrAction;
    private final StructureExtractionAction structureExtractionAction;
    private final SummaryAction summaryAction;
    private final EmbeddingAction embeddingAction;
    private final CompletionAction completionAction;
    private final FailureAction failureAction;

    public PipelineStateMachineConfig(OcrAction ocrAction, StructureExtractionAction structureExtractionAction, SummaryAction summaryAction, EmbeddingAction embeddingAction, CompletionAction completionAction, FailureAction failureAction) {
        this.ocrAction = ocrAction;
        this.structureExtractionAction = structureExtractionAction;
        this.summaryAction = summaryAction;
        this.embeddingAction = embeddingAction;
        this.completionAction = completionAction;
        this.failureAction = failureAction;
    }

    @Override
    public void configure(StateMachineStateConfigurer<PipelineState, PipelineEvent> states) throws Exception {
        states
                .withStates()
                .initial(PipelineState.IDLE)
                .states(EnumSet.allOf(PipelineState.class))
                .state(PipelineState.OCR, ocrAction)
                .state(PipelineState.STRUCTURE_EXTRACTION, structureExtractionAction)
                .state(PipelineState.SUMMARY, summaryAction)
                .state(PipelineState.EMBEDDING, embeddingAction)
                .state(PipelineState.COMPLETED, completionAction)
                .state(PipelineState.FAILED, failureAction)
                .end(PipelineState.COMPLETED)
                .end(PipelineState.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PipelineState, PipelineEvent> transitions) throws Exception {
        transitions
                // OCR -> STRUCTURE_EXTRACTION -> SUMMARY -> EMBEDDING
                .withExternal()
                .source(PipelineState.IDLE).target(PipelineState.OCR).event(PipelineEvent.TO_OCR)
                .and()
                .withExternal()
                .source(PipelineState.OCR).target(PipelineState.STRUCTURE_EXTRACTION).event(PipelineEvent.TO_STRUCTURE_EXTRACTION)
                .and()
                .withExternal()
                .source(PipelineState.STRUCTURE_EXTRACTION).target(PipelineState.SUMMARY).event(PipelineEvent.TO_SUMMARY)
                .and()
                .withExternal()
                .source(PipelineState.SUMMARY).target(PipelineState.EMBEDDING).event(PipelineEvent.TO_EMBEDDING)
                .and()
                .withExternal()
                .source(PipelineState.STRUCTURE_EXTRACTION).target(PipelineState.COMPLETED).event(PipelineEvent.TO_COMPLETED)
                .and()
                .withExternal()
                .source(PipelineState.EMBEDDING).target(PipelineState.COMPLETED).event(PipelineEvent.TO_COMPLETED)
                .and()
                // 错误处理
                .withExternal()
                .source(PipelineState.OCR).target(PipelineState.FAILED).event(PipelineEvent.TO_FAILED)
                .and()
                .withExternal()
                .source(PipelineState.STRUCTURE_EXTRACTION).target(PipelineState.FAILED).event(PipelineEvent.TO_FAILED)
                .and()
                .withExternal()
                .source(PipelineState.SUMMARY).target(PipelineState.FAILED).event(PipelineEvent.TO_FAILED)
                .and()
                .withExternal()
                .source(PipelineState.EMBEDDING).target(PipelineState.FAILED).event(PipelineEvent.TO_FAILED);
    }
}
