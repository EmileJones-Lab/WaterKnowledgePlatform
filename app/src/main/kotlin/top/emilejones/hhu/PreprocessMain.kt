package top.emilejones.hhu

import top.emilejones.hhu.spliter.HtmlTableSplitter
import top.emilejones.hhu.spliter.PunctuationSplitter
import top.emilejones.huu.env.RAGEnvironment
import java.io.File

fun main() {
    val outputFile = File("/Users/sunhongfei/Downloads/测试文档/淮河水资源调度方案（非最终稿）/淮河水资源调度方案.md")
    val sourceFile = File("/Users/sunhongfei/Downloads/测试文档/淮河水资源调度方案（非最终稿）/淮河水资源调度方案（非最终稿）.md")

    val processedText = sourceFile.readText().lines()
        .map { it.trimIndent() }
        .filter { it.isNotBlank() }
        .map {
            if (it.startsWith("<table>")) {
            val splitResult = HtmlTableSplitter.split(it, RAGEnvironment.MAX_TABLE_LENGTH).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            if (it.length > RAGEnvironment.MAX_SEQUENCE_LENGTH){
                val splitResult = PunctuationSplitter.split(it,RAGEnvironment.MAX_SEQUENCE_LENGTH).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            listOf(it)
        }.flatten()
        .joinToString(separator = "\n")

    outputFile.createNewFile()
    outputFile.writeText(processedText)
}