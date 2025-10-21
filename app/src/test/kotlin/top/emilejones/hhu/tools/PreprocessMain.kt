package top.emilejones.hhu.tools

import kotlinx.coroutines.*
import top.emilejones.hhu.spliter.impl.HtmlTableSplitter
import top.emilejones.hhu.spliter.impl.PunctuationSplitter
import top.emilejones.huu.env.AutoFindConfigFile
import java.io.File

val config = AutoFindConfigFile.find()
fun main(): Unit = runBlocking {
    val sourceDir = File("/Users/sunhongfei/Downloads/test")
    val targetDir = File("/Users/sunhongfei/Downloads/test-result")
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
        .asSequence()
        .map { it.trimIndent() }
        .filter { it.isNotBlank() }
        .map {
            if (it.startsWith("<table>") && it.length > config.rag.maxTableLength) {
                val splitResult = HtmlTableSplitter.split(it, config.rag.maxTableLength).getOrNull()
                splitResult!!.forEach { r -> if (r.length>1000) System.err.println(it) }
                return@map splitResult ?: emptyList<String>()
            }
            if (it.length > config.rag.maxSentenceLength) {
                val splitResult = PunctuationSplitter.split(it, config.rag.maxSentenceLength).getOrNull()
                return@map splitResult ?: emptyList<String>()
            }
            listOf(it)
        }.flatten()
        .joinToString(separator = "\n")
//    processedText.split("\n").forEach { if (it.length > 600) println("${sourceFile.name}[${it.length}]: $it") }

    outputFile.createNewFile()
    outputFile.writeText(processedText)
}