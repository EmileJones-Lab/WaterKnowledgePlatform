package top.yeyezhi.hhu.preprocessing.structure.enums;

/**
 * 标题类型枚举
 *
 * @author EmileJones
 */
public enum TitleType {
    NilType(null, null, "头节点，为了让算法逻辑一致而存在");

    // 标题的正则表达式
    private final String titleRegex;
    // 该类型标题的第一个标题
    private final String firstTitleRegex;
    // 备注说明
    private final String common;

    TitleType(String titleRegex, String firstTitleRegex, String common) {
        this.titleRegex = titleRegex;
        this.firstTitleRegex = firstTitleRegex;
        this.common = common;
    }

    public String getTitleRegex() {
        return titleRegex;
    }

    public String getCommon() {
        return common;
    }

    public String getFirstTitleRegex() {
        return firstTitleRegex;
    }
}
