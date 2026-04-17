package top.emilejones.hhu.application.platform.statemachine;

public enum PipelineState {
    IDLE,
    OCR,
    STRUCTURE_EXTRACTION,
    SUMMARY,
    EMBEDDING,
    COMPLETED,
    FAILED
}
