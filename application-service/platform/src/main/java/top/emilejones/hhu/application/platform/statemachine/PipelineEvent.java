package top.emilejones.hhu.application.platform.statemachine;

public enum PipelineEvent {
    TO_OCR,
    TO_STRUCTURE_EXTRACTION,
    TO_EMBEDDING,
    TO_COMPLETED,
    TO_FAILED
}
