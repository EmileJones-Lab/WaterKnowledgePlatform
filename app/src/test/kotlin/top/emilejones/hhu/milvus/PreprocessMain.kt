package top.emilejones.hhu.milvus

import kotlinx.coroutines.*
import top.emilejones.hhu.spliter.HtmlTableSplitter
import top.emilejones.hhu.spliter.PunctuationSplitter
import top.emilejones.huu.env.AutoFindConfigFile
import java.io.File

val config = AutoFindConfigFile.find()
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
                val splitResult = HtmlTableSplitter.split(it, config.rag.maxTableLength).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            if (it.length > config.rag.maxSequenceLength) {
                val splitResult = PunctuationSplitter.split(it, config.rag.maxSequenceLength).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            listOf(it)
        }.flatten()
        .joinToString(separator = "\n")

    outputFile.createNewFile()
    outputFile.writeText(processedText)
}