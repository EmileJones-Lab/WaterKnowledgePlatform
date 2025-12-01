package top.emilejones.hhu.domain.knowledge

/**
 * 知识库目录类型，标识切分方式差异。
 * @author EmileJones
 */
enum class KnowledgeCatalogType(val comment: String) {
    CHAR_NUMBER_SPLIT_DIR("根据字符长度切割的知识库"),
    STRUCTURE_KNOWLEDGE_DIR("基于文本层次结构的知识库")
}
