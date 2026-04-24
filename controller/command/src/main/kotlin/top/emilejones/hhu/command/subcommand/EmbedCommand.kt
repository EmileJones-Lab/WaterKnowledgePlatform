package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.EmbeddingApplicationService
import top.emilejones.hhu.command.util.progress.BatchProgressManager
import top.emilejones.hhu.command.util.progress.Spinner
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

        when {
            file.isDirectory -> processDirectory(file)
            file.isFile && file.extension.equals("md", ignoreCase = true) -> processSingleFile(file)
            else -> echo("Provided path is not a Markdown file or directory: $source", err = true)
        }
    }

    /**
     * 处理单个 Markdown 文件，使用 Spinner 指示处理状态。
     */
    private fun processSingleFile(file: File) {
        val spinner = Spinner("Vectorizing ${file.name}...")
        spinner.start()

        try {
            embeddingApplicationService.embed(file.absolutePath)
            echo("Successfully vectorized: ${file.absolutePath}")
        } catch (e: Exception) {
            echo("Error during vectorization for ${file.absolutePath}: ${e.message}", err = true)
        } finally {
            spinner.stop()
        }
    }

    /**
     * 批量处理目录下的所有 Markdown 文件，使用基于完成计数的真实进度条。
     */
    private fun processDirectory(directory: File) {
        val mdFiles = directory.walkTopDown()
            .filter { it.isFile && it.extension.equals("md", ignoreCase = true) }
            .toList()

        if (mdFiles.isEmpty()) {
            echo("No Markdown files found in directory: ${directory.absolutePath}")
            return
        }

        echo("Found ${mdFiles.size} Markdown files. Starting batch vectorization...")

        val batchManager = BatchProgressManager(mdFiles.size)
        batchManager.start()

        runBlocking {
            mdFiles.map { mdFile ->
                launch(Dispatchers.IO) {
                    batchManager.addTask(mdFile.name)

                    try {
                        embeddingApplicationService.embed(mdFile.absolutePath)
                    } catch (e: Exception) {
                        echo("Error during vectorization for ${mdFile.absolutePath}: ${e.message}", err = true)
                    } finally {
                        batchManager.completeTask(mdFile.name)
                    }
                }
            }.joinAll()
        }
    }
}
