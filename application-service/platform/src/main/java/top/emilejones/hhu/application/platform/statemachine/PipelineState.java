package top.emilejones.hhu.application.platform.statemachine;

public enum PipelineState {
    IDLE,
    OCR,
    STRUCTURE_EXTRACTION,
    EMBEDDING,
    COMPLETED,
    FAILED
}
