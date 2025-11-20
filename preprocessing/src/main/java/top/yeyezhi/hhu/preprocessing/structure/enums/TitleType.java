package top.yeyezhi.hhu.preprocessing.structure.enums;

/**
 * 标题类型枚举
 *
 * @author EmileJones
 */
public enum TitleType {
    NilType(null, null, "头节点，为了让算法逻辑一致而存在"),
    TYPE_HIERARCHICAL_4("^[1-9][0-9]*\\.[1-9][0-9]*\\.[1-9][0-9]*\\.[1-9][0-9]*[\\s].*", "^1\\.1\\.1\\.1[\\s].*", "层级标题，例如：1.1.1.1"),
    TYPE_HIERARCHICAL_3("^[1-9][0-9]*\\.[1-9][0-9]*\\.[1-9][0-9]*[\\s].*", "^1\\.1\\.1[\\s].*", "层级标题，例如：1.1.1"),
    TYPE_HIERARCHICAL_2("^[1-9][0-9]*\\.[1-9][0-9]*[\\s].*", "^1\\.1[\\s].*", "层级标题，例如：1.1"),
    TYPE_CHINESE_NUMBER("^[一二三四五六七八九十百千万]+[、\\.][\\s].*", "^一[、\\.][\\s].*", "中文数字标题，例如：一、"),
    TYPE_NUMBER_DOT("^[1-9][0-9]*\\.[\\s].*", "^1\\.[\\s].*", "数字加点，例如：1."),
    TYPE_PARENTHESIZED_NUMBER("^[（(][1-9][0-9]*[)）][\\s].*", "^[（(]1[)）][\\s].*", "括号数字，例如：(1) 或 （1）"),
    TYPE_CHINESE_PARENTHESIZED_NUMBER("^[（(][一二三四五六七八九十百千万]+[)）][\\s].*", "^[（(]一[)）][\\s].*", "括号中文数字，例如：(一) 或 （一）"),
    TYPE_NUMBER_PAREN("^[1-9][0-9]*[)）][\\s].*", "^1[)）][\\s].*", "数字加右括号，例如：1) 或 1）");


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