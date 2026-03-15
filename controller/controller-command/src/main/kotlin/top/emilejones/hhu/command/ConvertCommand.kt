package top.emilejones.hhu.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.CommandApplicationService
import top.emilejones.hhu.application.command.dto.MinerUMarkdownResponse
import java.io.File

/**
 * 转换命令：将 PDF 转换为 Markdown。
 * 支持本地路径和远程 URL。
 */
@Component
class ConvertCommand(
    private val commandApplicationService: CommandApplicationService
) : CliktCommand(name = "convert") {

    private val source by argument(help = "The URL or local path of the PDF file")
    private val outputDir by option("-o", "--output", help = "The directory to save the output files")

    override fun help(context: Context): String = "Convert a PDF file to Markdown with images"

    override fun run() {
        val file = File(source)
        if (file.exists() && file.isDirectory) {
            echo("Scanning directory: $source")
            val pdfFiles = file.walkTopDown()
                .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
                .toList()

            if (pdfFiles.isEmpty()) {
                echo("No PDF files found in directory: $source")
                return
            }

            echo("Found ${pdfFiles.size} PDF files. Starting batch conversion...")
            pdfFiles.forEach { pdfFile ->
                processSource(pdfFile.absolutePath)
            }
        } else {
            processSource(source)
        }
    }

    /**
     * 执行单个源的转换逻辑。
     */
    private fun processSource(src: String) {
        echo("Starting conversion for: $src")

        try {
            // 1. 调用应用服务执行转换
            val response = commandApplicationService.extractStructure(src)
            echo("Conversion successful: $src")

            // 2. 如果指定了输出目录，则保存文件
            outputDir?.let { dirPath ->
                val fileName = if (src.contains("://")) {
                    src.substringAfterLast('/')
                } else {
                    File(src).name
                }
                saveResults(fileName, response, dirPath)
                echo("Results for $fileName saved to: ${File(dirPath).normalize().absolutePath}")
            } ?: run {
                echo("No output directory specified for $src. Process finished without saving.")
            }

        } catch (e: Exception) {
            echo("Error during conversion for $src: ${e.message}", err = true)
        }
    }

    /**
     * 将转换结果保存到本地磁盘。
     */
    private fun saveResults(fileName: String, response: MinerUMarkdownResponse, dirPath: String) {
        val baseDir = File(dirPath)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        // 1. 保存 Markdown 文件
        val mdFile = File(baseDir, "${fileName.substringBeforeLast('.')}.md")
        mdFile.writeText(response.markdownContent)
        echo(fileName)
        echo(mdFile.absolutePath)
        // 2. 保存图片文件
        response.images.forEach { image ->
            // image.relativePath 格式通常为 "images/xxx.png"
            val imageFile = File(baseDir, image.relativePath)

            // 确保图片所在的父目录存在
            imageFile.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }

            // 写入二进制数据
            imageFile.writeBytes(image.data)
        }
    }
}