package top.emilejones.hhu.preprocessing.handler

/**
 * 统一处理markdown文件的约束
 *
 * @author EmileJones
 */
@Deprecated("旧的设计无法实现很好的效果，已经启用")
interface MarkdownFileHandler {

    fun handle(markdownText: String): String
}