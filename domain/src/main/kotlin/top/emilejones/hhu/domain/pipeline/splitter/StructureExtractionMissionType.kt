package top.emilejones.hhu.domain.pipeline.splitter

enum class StructureExtractionMissionType(val comment: String) {
    CHAR_LENGTH_SPLITTER_200("根据200个字符长度切割"),
    CHAR_LENGTH_SPLITTER_400("根据400个字符长度切割"),
    CHAR_LENGTH_SPLITTER_600("根据600个字符长度切割"),
    STRUCTURE_SPLITTER("根据文本结构去切割"),
}