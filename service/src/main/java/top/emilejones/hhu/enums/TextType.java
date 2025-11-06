package top.emilejones.hhu.enums;

/**
 * 文本类型枚举
 *
 * @author EmileJones
 */
public enum TextType {
    COMMON_TEXT("普通文本"),
    TABLE("表格文本"),
    IMAGE("图片文本"),
    TITLE("标题文本");

    private final String comment;

    TextType(String comment) {
        this.comment = comment;
    }
}
