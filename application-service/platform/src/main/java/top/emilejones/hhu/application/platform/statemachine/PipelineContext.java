package top.emilejones.hhu.application.platform.statemachine;

import lombok.Builder;
import lombok.Data;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

@Data
@Builder
public class PipelineContext {
    private String sourceDocumentId;
    private PipelineState targetState;
    
    private OcrMission ocrMission;
    private StructureExtractionMission structureExtractionMission;
    private EmbeddingMission embeddingMission;
    
    // 用于标记当前正在处理的顶级任务ID，以便在失败时更新状态
    private String currentTopMissionId;
}
