package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.StructureExtractApplicationService
import top.emilejones.hhu.command.util.progress.BatchProgressManager
import top.emilejones.hhu.command.util.progress.Spinner
import java.io.File

/**
 * 结构提取命令：将 Markdown 文件转换为图结构。
 * 支持本地路径和文件夹路径（递归扫描）。
 */
@Component
class ExtractCommand(
    private val structureExtractApplicationService: StructureExtractApplicationService
) : CliktCommand(name = "extract") {

    private val source by argument(help = "The local path of the Markdown file or directory")

    private val withSummary by option("--with-summary", "-s", help = "Also extract summary after structure extraction")
        .flag(default = false)

    override fun help(context: Context): String = "Extract structure from Markdown files and store into graph database"

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
        val spinner = Spinner("Extracting ${file.name}...")
        spinner.start()

        try {
            structureExtractApplicationService.extractTextStructure(file.absolutePath)
            if (withSummary) {
                structureExtractApplicationService.extractSummary(file.absolutePath)
            }
            echo("Successfully extracted structure for: ${file.absolutePath}")
        } catch (e: Exception) {
            echo("Error during extraction for ${file.absolutePath}: ${e.message}", err = true)
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

        echo("Found ${mdFiles.size} Markdown files. Starting batch extraction...")

        val batchManager = BatchProgressManager(mdFiles.size)
        batchManager.start()

        runBlocking {
            mdFiles.map { mdFile ->
                launch(Dispatchers.IO) {
                    batchManager.addTask(mdFile.name)

                    try {
                        structureExtractApplicationService.extractTextStructure(mdFile.absolutePath)
                        if (withSummary) {
                            structureExtractApplicationService.extractSummary(mdFile.absolutePath)
                        }
                    } catch (e: Exception) {
                        echo("Error during extraction for ${mdFile.absolutePath}: ${e.message}", err = true)
                    } finally {
                        batchManager.completeTask(mdFile.name)
                    }
                }
            }.joinAll()
        }
    }
}
