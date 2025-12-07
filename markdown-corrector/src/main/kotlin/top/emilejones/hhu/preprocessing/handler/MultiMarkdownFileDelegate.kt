package top.emilejones.hhu.preprocessing.handler

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.relativeTo

/**
 * 此类可以批量处理markdown文件
 * 如果传入的是文件夹，则处理该文件夹下所有以`.md`为结尾的文件
 * 此类被设计出来时，不可以传入单个文件，传入单个文件路径可能会发生错误
 *
 * @author EmileJones
 */
class MultiMarkdownFileDelegate(filePath: String, targetRootDirPath: String) {
    private val originFile: File = File(filePath)
    private val targetRootDirPath = Path.of(targetRootDirPath).toAbsolutePath().normalize()
    private val originFilePath: Path = Path.of(filePath).toAbsolutePath().normalize()
    private val handlerList: MutableList<MarkdownFileHandler> = ArrayList()

    fun addHandler(handler: MarkdownFileHandler): MultiMarkdownFileDelegate {
        handlerList.add(handler)
        return this
    }

    fun run() {
        originFile.walk().forEach { handleTo(it) }
    }

    private fun handleTo(file: File) {
        if (file.isDirectory) {
            return
        }
        val suffix = file.name.split('.').last()
        if ("md" == suffix.lowercase())
            handleMD(file)
        else
            handleOtherFile(file)
    }

    private fun handleMD(mdFile: File) {
        var markdownFileText = mdFile.readText(Charsets.UTF_8)

        for (handler in handlerList) {
            markdownFileText = handler.handle(markdownFileText)
        }

        saveFileTo(markdownFileText.toByteArray(Charsets.UTF_8), getRelativePathFromOriginPath(mdFile.path))
    }

    private fun handleOtherFile(otherFile: File) {
        saveFileTo(otherFile.readBytes(), getRelativePathFromOriginPath(otherFile.path))
    }

    private fun saveFileTo(byteArray: ByteArray, relativePath: Path) {
        val file = targetRootDirPath.resolve(relativePath).toFile()
        Files.createDirectories(file.parentFile.toPath())
        file.writeBytes(byteArray)
    }

    private fun getRelativePathFromOriginPath(filePath: String): Path {
        val targetFilePath = Path.of(filePath).toAbsolutePath().normalize()
        return targetFilePath.relativeTo(originFilePath)
    }
}