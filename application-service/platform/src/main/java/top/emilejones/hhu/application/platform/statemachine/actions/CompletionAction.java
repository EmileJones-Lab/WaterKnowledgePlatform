package top.emilejones.hhu.application.platform.statemachine.actions;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.application.platform.statemachine.PipelineContext;
import top.emilejones.hhu.application.platform.statemachine.PipelineEvent;
import top.emilejones.hhu.application.platform.statemachine.PipelineState;
import top.emilejones.hhu.domain.document.SourceDocument;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;

@Component
public class CompletionAction implements Action<PipelineState, PipelineEvent> {
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final SourceDocumentRepository sourceDocumentRepository;

    public CompletionAction(KnowledgeDocumentRepository knowledgeDocumentRepository, SourceDocumentRepository sourceDocumentRepository) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
    }

    @Override
    public void execute(StateContext<PipelineState, PipelineEvent> context) {
        PipelineContext pipelineContext = context.getExtendedState().get("context", PipelineContext.class);

        // 如果最终目标是向量化且成功，则创建知识文档
        if (pipelineContext.getTargetState() == PipelineState.EMBEDDING && pipelineContext.getEmbeddingMission() != null) {
            EmbeddingMission embeddingMission = pipelineContext.getEmbeddingMission();
            // 如果Embedding失败，则什么也不做
            if (!embeddingMission.isSuccess()) return;
            // 如果Embedding成功，则产生知识文件
            String sourceDocName = sourceDocumentRepository.findSourceDocumentById(embeddingMission.getSourceDocumentId())
                    .map(SourceDocument::getName)
                    .orElse("Unknown File");

            KnowledgeDocument doc = KnowledgeDocument.Companion.create(
                    sourceDocName,
                    embeddingMission.getId(),
                    KnowledgeDocumentType.STRUCTURE_SPLITTER
            );
            knowledgeDocumentRepository.save(doc);
        }
    }
}
