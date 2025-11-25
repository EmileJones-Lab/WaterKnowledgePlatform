package top.emilejones.hhu.web.vo.mission.enums;

public enum DocumentSplittingMissionType {
    CHAR_LENGTH_SPLITTER_200("根据200个字符长度切割"),
    CHAR_LENGTH_SPLITTER_400("根据400个字符长度切割"),
    CHAR_LENGTH_SPLITTER_600("根据600个字符长度切割"),
    STRUCTURE_SPLITTER("根据文本结构去切割");

    private final String comment;

    DocumentSplittingMissionType(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
