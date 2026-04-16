package top.emilejones.hhu.application.platform.statemachine.actions;

import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.application.platform.statemachine.PipelineContext;
import top.emilejones.hhu.application.platform.statemachine.PipelineEvent;
import top.emilejones.hhu.application.platform.statemachine.PipelineState;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

@Component
public class FailureAction implements Action<PipelineState, PipelineEvent> {
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;

    public FailureAction(EmbeddingMissionRepository embeddingMissionRepository, StructureExtractionMissionRepository structureExtractionMissionRepository) {
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
    }

    @Override
    public void execute(org.springframework.statemachine.StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);
        String topMissionId = pipelineContext.getCurrentTopMissionId();
        PipelineState targetState = pipelineContext.getTargetState();

        if (targetState == PipelineState.EMBEDDING) {
            EmbeddingMission mission = embeddingMissionRepository.find(topMissionId);
            if (mission != null && !mission.isCompleted()) {
                mission.failure("流水线前置任务失败");
                embeddingMissionRepository.save(mission);
            }
        } else if (targetState == PipelineState.STRUCTURE_EXTRACTION) {
            StructureExtractionMission mission = structureExtractionMissionRepository.find(topMissionId);
            if (mission != null && !mission.isCompleted()) {
                mission.failure("流水线前置任务失败");
                structureExtractionMissionRepository.save(mission);
            }
        }
    }
}
