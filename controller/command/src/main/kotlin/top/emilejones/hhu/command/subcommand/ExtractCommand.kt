package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.StructureExtractApplicationService
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

    override fun help(context: Context): String = "Extract structure from Markdown files and store into graph database"

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

            echo("Found ${mdFiles.size} Markdown files. Starting batch extraction...")
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
        echo("Starting structure extraction for: $filePath")
        try {
            structureExtractApplicationService.extractStructure(filePath)
            echo("Successfully extracted structure for: $filePath")
        } catch (e: Exception) {
            echo("Error during extraction for $filePath: ${e.message}", err = true)
        }
    }
}
