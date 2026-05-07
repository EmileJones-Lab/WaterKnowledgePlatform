package top.emilejones.hhu.command.subcommand

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import top.emilejones.hhu.application.command.OcrApplicationService
import top.emilejones.hhu.application.command.dto.MinerUMarkdownResponse
import top.emilejones.hhu.command.util.progress.BatchProgressManager
import top.emilejones.hhu.command.util.progress.Spinner
import java.io.File

/**
 * 转换命令：将 PDF 转换为 Markdown。
 * 支持本地路径和远程 URL。
 */
@Component
class ConvertCommand(
    private val ocrApplicationService: OcrApplicationService
) : CliktCommand(name = "convert") {

    private val source by argument(help = "The URL or local path of the PDF file")
    private val outputDir by option("-o", "--output", help = "The directory to save the output files").required()
    private val dryRun by option("-n", "--dry-run", help = "Run without generating any output files").flag()

    override fun help(context: Context): String = "Convert a PDF file to Markdown with images"

    override fun run() {
        val file = File(source)
        if (file.exists() && file.isDirectory) {
            processDirectory(file)
        } else {
            processSingleSource(source)
        }
    }

    /**
     * 处理单个 PDF 源（本地文件或 URL），使用 Spinner 指示处理状态。
     */
    private fun processSingleSource(src: String) {
        val label = File(src).let { if (it.exists() && it.isFile) it.name else src.substringAfterLast('/') }
        val spinner = Spinner("Converting $label...")
        spinner.start()

        try {
            val response = ocrApplicationService.extractStructure(src)
            handleConversionResult(src, response)
        } catch (e: Exception) {
            val errorMsg = e.cause?.let { "${e.message} (Cause: ${it.message})" } ?: e.message
            echo("Error during conversion for $src: $errorMsg", err = true)
        } finally {
            spinner.stop()
        }
    }

    /**
     * 批量处理目录下的所有 PDF 文件，使用基于完成计数的真实进度条。
     */
    private fun processDirectory(directory: File) {
        val pdfFiles = directory.walkTopDown()
            .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            .toList()

        if (pdfFiles.isEmpty()) {
            echo("No PDF files found in directory: ${directory.absolutePath}")
            return
        }

        echo("Found ${pdfFiles.size} PDF files. Starting batch conversion...")

        val batchManager = BatchProgressManager(pdfFiles.size)
        batchManager.start()

        runBlocking {
            pdfFiles.map { pdfFile ->
                launch(Dispatchers.IO) {
                    batchManager.addTask(pdfFile.name)

                    try {
                        val response = ocrApplicationService.extractStructure(pdfFile.absolutePath)
                        handleConversionResult(pdfFile.absolutePath, response)
                    } catch (e: Exception) {
                        val errorMsg = e.cause?.let { "${e.message} (Cause: ${it.message})" } ?: e.message
                        echo("Error during conversion for ${pdfFile.absolutePath}: $errorMsg", err = true)
                    } finally {
                        batchManager.completeTask(pdfFile.name)
                    }
                }
            }.joinAll()
        }
    }

    /**
     * 处理转换结果：保存文件。
     */
    private fun handleConversionResult(src: String, response: MinerUMarkdownResponse) {
        if (dryRun) {
            echo("Dry run completed for: $src")
            return
        }
        val dirPath = outputDir
        val fileName = if (src.contains("://")) {
            src.substringAfterLast('/')
        } else {
            File(src).name
        }
        saveResults(fileName, response, dirPath)
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
