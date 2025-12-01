package top.emilejones.hhu.preprocessing.handler

/**
 * 统一处理markdown文件的约束
 *
 * @author EmileJones
 */
interface MarkdownFileHandler {

    fun handle(markdownText: String): String
}