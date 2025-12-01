package top.emilejones.hhu.domain.knowledge

enum class KnowledgeCatalogType(val comment: String) {
    CHAR_NUMBER_SPLIT_DIR("根据字符长度切割的知识库"),
    STRUCTURE_KNOWLEDGE_DIR("基于文本层次结构的知识库")
}