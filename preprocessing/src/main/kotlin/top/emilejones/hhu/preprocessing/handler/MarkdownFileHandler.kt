package top.emilejones.hhu.preprocessing.handler

interface MarkdownFileHandler {

    fun handle(markdownText: String): String
}