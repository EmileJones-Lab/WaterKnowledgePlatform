package top.emilejones.hhu

import kotlinx.coroutines.*
import top.emilejones.hhu.spliter.HtmlTableSplitter
import top.emilejones.hhu.spliter.PunctuationSplitter
import top.emilejones.huu.env.RAGEnvironment
import java.io.File

fun main(): Unit = runBlocking {
    val sourceDir = File("/Users/sunhongfei/Downloads/out")
    val targetDir = File("/Users/sunhongfei/Downloads/测试文档")
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    val job = sourceDir.walk().mapNotNull {
        if (it.isDirectory)
            return@mapNotNull null
        if (it.name.split('.').last().lowercase() != "md")
            return@mapNotNull null
        val outputFile = targetDir.toPath().resolve("./${it.name}").toFile()
        if (!outputFile.exists())
            outputFile.createNewFile()

        scope.launch {
            saveFile(it, outputFile)
        }
    }.toList()

    job.joinAll()
}

private fun saveFile(sourceFile: File, outputFile: File) {
    val processedText = sourceFile.readText().lines()
        .map { it.trimIndent() }
        .filter { it.isNotBlank() }
        .map {
            if (it.startsWith("<table>")) {
                val splitResult = HtmlTableSplitter.split(it, RAGEnvironment.MAX_TABLE_LENGTH).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            if (it.length > RAGEnvironment.MAX_SEQUENCE_LENGTH) {
                val splitResult = PunctuationSplitter.split(it, RAGEnvironment.MAX_SEQUENCE_LENGTH).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            listOf(it)
        }.flatten()
        .joinToString(separator = "\n")

    outputFile.createNewFile()
    outputFile.writeText(processedText)
}