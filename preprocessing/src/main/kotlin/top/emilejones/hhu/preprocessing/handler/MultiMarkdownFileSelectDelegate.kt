package top.emilejones.hhu.preprocessing.handler

import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector2
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector3
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector4
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector5
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector2
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector3
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector4
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.relativeTo

/**
 * 此类可以批量处理markdown文件，根据markdown文件的后缀名去使用不同的策略
 *
 * ## Attention
 * 此类被设计出来时，不可以传入单个文件，传入单个文件路径可能会发生错误
 *
 * @author yeyezhi
 */
class MultiMarkdownFileSelectDelegate(filePath: String, targetRootDirPath: String) {
    private val originFile: File = File(filePath)
    private val targetRootDirPath = Path.of(targetRootDirPath).toAbsolutePath().normalize()
    private val originFilePath: Path = Path.of(filePath).toAbsolutePath().normalize()
    // private val handlerList: MutableList<MarkdownFileHandler> = ArrayList()

//    fun addHandler(handler: MarkdownFileHandler): MultiMarkdownFileSelectDelegate {
//        handlerList.add(handler)
//        return this
//    }

    fun run() {
        originFile.walk().forEach { file ->
            if (file.isDirectory) return@forEach
            val suffix = file.extension.lowercase()
            if (suffix == "md") {
                handleMD(file)
            } else {
                handleOtherFile(file)
            }
        }
    }

//    private fun handleTo(file: File) {
//        if (file.isDirectory) {
//            return
//        }
//        val suffix = file.name.split('.').last()
//        if ("md".equals(suffix.lowercase()))
//            handleMD(file)
//        else
//            handleOtherFile(file)
//    }

    private fun handleMD(mdFile: File) {
        val parentName = mdFile.parentFile.parentFile.name  // 拿到最外层文件夹名
        val handler: MarkdownFileHandler? = when {
            parentName.endsWith("222") || parentName.endsWith("444") || parentName.endsWith("555") || parentName.endsWith("999") -> {
                MarkdownStructureCorrector2()
            }
            parentName.endsWith("13") -> {
                MarkdownStructureCorrector4()
            }
            parentName.endsWith("14") -> {
                MixedStructureCorrector4()
            }
            parentName.endsWith("15") -> {
                MarkdownStructureCorrector5()
            }
            parentName.endsWith("111") -> {
                MixedStructureCorrector()
            }
            parentName.endsWith("333") -> {
                MarkdownStructureCorrector3()
            }
            parentName.endsWith("888") -> {
                MixedStructureCorrector2()
            }
            parentName.endsWith("11") -> {
                MixedStructureCorrector3()
            }

            else -> null  // 其他文件不处理
        }

        var markdownFileText = mdFile.readText(Charsets.UTF_8)

        if (handler != null) {
            markdownFileText = handler.handle(markdownFileText)
        }
        // 不管有没有处理，都要保存到 output
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