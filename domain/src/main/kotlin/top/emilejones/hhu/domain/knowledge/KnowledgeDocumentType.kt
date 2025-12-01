package top.emilejones.hhu.domain.knowledge

/**
 * 知识文档类型，体现切分粒度与策略。
 * @author EmileJones
 */
enum class KnowledgeDocumentType(val comment: String) {
    CHAR_LENGTH_SPLITTER_200("根据200个字符长度切割的知识文档"),
    CHAR_LENGTH_SPLITTER_400("根据400个字符长度切割的知识文档"),
    CHAR_LENGTH_SPLITTER_600("根据600个字符长度切割的知识文档"),
    STRUCTURE_SPLITTER("根据文本结构去切割的知识文档"),
}
