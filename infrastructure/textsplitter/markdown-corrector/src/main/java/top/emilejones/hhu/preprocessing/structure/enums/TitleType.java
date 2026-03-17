package top.emilejones.hhu.preprocessing.structure.enums;

/**
 * 标题类型枚举
 *
 * @author EmileJones
 * @author yeyezhi
 */
// 添加尽可能多的正则表达式
public enum TitleType {
    NilType(null, null, "头节点，为了让算法逻辑一致而存在"),
    TYPE_HIERARCHICAL_4("^(?:#+\\s*)?([0-9]{1,2}(?:\\.[0-9]{1,2}){3,4}\\.?)(?![0-9]{1,2}(?![0-9])|0)(?![\\.．。][0-9])\\s*(\\S.*)$",
            "^(?:#+\\s*)?[0-9]{1,2}(?:\\.[0-9]{1,2}){2,3}\\.1\\.?(?![0-9]{1,2}(?![0-9])|0)(?![\\.．。][0-9])\\s*(\\S.*)$",
            "四级标题 1.1.1.1"),
    TYPE_HIERARCHICAL_3("^(?:#+\\s*)?(\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}\\.?)(?:\\s+|(?<=\\.)(?!\\d{1,2}(?!\\d))|(?<!\\.)(?!\\d))(?![\\.．。]\\d{1,2}(?!\\d))(\\S.*)$",
            "^(?:#+\\s*)?\\d{1,2}\\.\\d{1,2}\\.1\\.?(?:\\s+|(?<=\\.)(?!\\d{1,2}(?!\\d))|(?<!\\.)(?!\\d))(?![\\.．。]\\d{1,2}(?!\\d))(\\S.*)$",
            "三级标题 '1.1.1' 和 '1.1.1.'"),
    TYPE_HIERARCHICAL_2("^(?:#+\\s*)?([0-9]{1,2}\\.[0-9]{1,2}(?:\\.(?![0-9]{1,2}(?![0-9]))|(?!\\.|\\d)))\\s*(\\S.*)$",
            "^(?:#+\\s*)?[0-9]{1,2}\\.1(?:\\.(?![0-9]{1,2}(?![0-9]))|(?!\\.|\\d))\\s*(\\S.*)$",
            "二级标题 1.1"),
    TYPE_CHINESE_NUMBER("^(?:#+\\s*)?([一二三四五六七八九十百千零〇两]{1,4})[、\\.．。]\\s*(.+)$",
            "^(?:#+\\s*)?一[、\\.．。]\\s*(.+)$",
            "中文数字标题 一、或 一."),
    TYPE_PARENTHESIZED_NUMBER("^(?:#+\\s*)?[（(]([1-9][0-9]*)[)）]\\s*(.+)$",
            "^(?:#+\\s*)?[（(]1[)）]\\s*(.+)$",
            "括号数字 (1)"),
    TYPE_CHINESE_PARENTHESIZED_NUMBER("^(?:#+\\s*)?（([一二三四五六七八九十]+)）\\s*(.+)$",
            "^(?:#+\\s*)?（一）\\s*(.+)$",
            "括号中文数字 （一）"),
    TYPE_ARABIC_NUMBER("^(?:#+\\s*)?([0-9]{1,2})(?:[．。、]\\s*|\\s+|\\.(?:\\s+|(?![0-9]{1,2}(?![0-9]))))(\\S.*)$",
            "^(?:#+\\s*)?1(?:[．。、]\\s*|\\s+|\\.(?:\\s+|(?![0-9]{1,2}(?![0-9]))))(\\S.*)$",
            "数字开头标题：1. / 1、 / 1 / 1标题 (限制1-2位数字，排除年份)"),
    TYPE_NUMBER_PAREN("^(?:#+\\s*)?([0-9]{1,3})）\\s*(.+)$",
            "^(?:#+\\s*)?1）\\s*(.+)$",
            "数字加右括号，例如：1）");


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