package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.EmbeddingApplicationService
import java.io.File

/**
 * 向量化子命令：将结构化提取后的文本块进行向量化处理。
 * 支持指定本地 Markdown 文件或文件夹路径（递归扫描）。
 */
@Component
class EmbedCommand(
    private val embeddingApplicationService: EmbeddingApplicationService
) : CliktCommand(name = "embed") {

    private val source by argument(help = "The local path of the Markdown file or directory to embed")

    override fun help(context: Context): String = "Vectorize extracted text chunks and store into vector database"

    override fun run() {
        val file = File(source)
        if (!file.exists()) {
            echo("Source does not exist: $source", err = true)
            return
        }

        if (file.isDirectory) {
            echo("Scanning directory: $source")
            val mdFiles = file.walkTopDown()
                .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
                .toList()

            if (mdFiles.isEmpty()) {
                echo("No Markdown files found in directory: $source")
                return
            }

            echo("Found ${mdFiles.size} Markdown files. Starting batch vectorization...")
            mdFiles.forEach { mdFile ->
                processFile(mdFile.absolutePath)
            }
        } else {
            if (file.extension.equals("md", ignoreCase = true)) {
                processFile(file.absolutePath)
            } else {
                echo("Provided file is not a Markdown file: $source", err = true)
            }
        }
    }

    /**
     * 调用应用服务处理单个文件。
     */
    private fun processFile(filePath: String) {
        echo("Starting vectorization for: $filePath")
        try {
            embeddingApplicationService.embed(filePath)
            echo("Successfully vectorized: $filePath")
        } catch (e: Exception) {
            echo("Error during vectorization for $filePath: ${e.message}", err = true)
        }
    }
}
