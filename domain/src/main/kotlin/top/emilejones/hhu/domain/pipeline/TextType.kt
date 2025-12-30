package top.emilejones.hhu.domain.pipeline

/**
 * 文本节点类型，区分内容形态。
 * @author EmileJones
 */
enum class TextType(val comment: String) {
    COMMON_TEXT("普通文本"),
    TABLE("表格文本"),
    IMAGE("图片文本"),
    TITLE("标题文本"),
    NULL("无效节点")
}
