package top.emilejones.hhu.web.vo.knowledge;

public enum KnowledgeDirectoryType {
    CHAR_NUMBER_SPLIT_DIR("根据字符长度切割的知识库"),
    STRUCTURE_KNOWLEDGE_DIR("基于文本层次结构的知识库");

    private final String comment;

    KnowledgeDirectoryType(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
