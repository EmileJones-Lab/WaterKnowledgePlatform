package top.emilejones.hhu.preprocessing.handler

import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector2
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector3
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector4
import top.yeyezhi.hhu.preprocessing.handler.MarkdownStructureCorrector5
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector2
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector3
import top.yeyezhi.hhu.preprocessing.handler.MixedStructureCorrector4
import top.yeyezhi.hhu.preprocessing.handler.structure.MarkdownStructureClassifier
import top.yeyezhi.hhu.preprocessing.handler.structure.StructurePatternLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.relativeTo

/**
 *  作用：批量处理 Markdown 文件并自动识别结构类型
 *
 *  功能说明：
 *      - 递归扫描输入目录下的所有文件
 *      - 对 Markdown 文件（.md）自动检测结构类型
 *      - 调用合适的 Corrector 进行结构层次修正
 *      - 将结果保存到目标目录中
 *  优点：
 *      无需手动标注文档类型后缀
 *      自动根据正则规则识别文档结构
 *      可扩展新结构模式（通过 YAML 文件）
 * @author yeyezhi
 */
class MultiMarkdownFileSelectDelegate(filePath: String, targetRootDirPath: String) {
    private val originFile: File = File(filePath)
    private val targetRootDirPath = Path.of(targetRootDirPath).toAbsolutePath().normalize()
    private val originFilePath: Path = Path.of(filePath).toAbsolutePath().normalize()
    private val logFile = File(targetRootDirPath.toString(), "process_log.txt")


    // private val handlerList: MutableList<MarkdownFileHandler> = ArrayList()

//    fun addHandler(handler: MarkdownFileHandler): MultiMarkdownFileSelectDelegate {
//        handlerList.add(handler)
//        return this
//    }

    /**
     * 主执行入口：递归扫描目录
     */
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

//    private fun handleMD(mdFile: File) {
//        val parentName = mdFile.parentFile.parentFile.name  // 拿到最外层文件夹名
//        val handler: MarkdownFileHandler? = when {
//            parentName.endsWith("222") || parentName.endsWith("444") || parentName.endsWith("555") || parentName.endsWith("999") -> {
//                MarkdownStructureCorrector2()
//            }
//            parentName.endsWith("13") -> {
//                MarkdownStructureCorrector4()
//            }
//            parentName.endsWith("14") -> {
//                MixedStructureCorrector4()
//            }
//            parentName.endsWith("15") -> {
//                MarkdownStructureCorrector5()
//            }
//            parentName.endsWith("111") -> {
//                MixedStructureCorrector()
//            }
//            parentName.endsWith("333") -> {
//                MarkdownStructureCorrector3()
//            }
//            parentName.endsWith("888") -> {
//                MixedStructureCorrector2()
//            }
//            parentName.endsWith("11") -> {
//                MixedStructureCorrector3()
//            }
//
//            else -> null  // 其他文件不处理
//        }
//
//        var markdownFileText = mdFile.readText(Charsets.UTF_8)
//
//        if (handler != null) {
//            markdownFileText = handler.handle(markdownFileText)
//        }
//        // 不管有没有处理，都要保存到 output
//        saveFileTo(markdownFileText.toByteArray(Charsets.UTF_8), getRelativePathFromOriginPath(mdFile.path))
//    }

    /**
     * 处理 Markdown 文件
     * 自动识别结构类型并调用对应处理器
     */
    private fun handleMD(mdFile: File) {
        // 路径过滤：只处理 auto 文件夹下的 .md 文件
        if (!mdFile.parentFile.name.equals("auto", ignoreCase = true)) {
            return
        }

        // ✅ 在控制台输出当前检测的文件名
        println("🔎 正在检测文件: ${mdFile.name}")

        // 读取文件内容
        var markdownFileText = mdFile.readText(Charsets.UTF_8)

        // 自动结构识别与分类
        val patterns = StructurePatternLoader.loadPatterns()
        val classifier = MarkdownStructureClassifier(patterns)
        val pattern = classifier.detect(markdownFileText)
        val handler = classifier.getHandler(pattern)

        if (pattern != null) {
            logFile.appendText("${mdFile.name} -> 结构类型 ${pattern.id} (${pattern.handler})\n")
        } else {
            logFile.appendText("${mdFile.name} -> 未匹配到结构类型\n")
        }

        // 执行对应的结构修正逻辑
        val corrected = handler?.handle(markdownFileText) ?: markdownFileText
        // 输出结果文件
        saveFileTo(corrected.toByteArray(Charsets.UTF_8), getRelativePathFromOriginPath(mdFile.path))
    }


    /**
     * 处理非 Markdown 文件（直接复制）
     */
    private fun handleOtherFile(otherFile: File) {
        saveFileTo(otherFile.readBytes(), getRelativePathFromOriginPath(otherFile.path))
    }

    /**
     * 将处理后的文件保存到目标路径
     */
    private fun saveFileTo(byteArray: ByteArray, relativePath: Path) {
        val file = targetRootDirPath.resolve(relativePath).toFile()
        Files.createDirectories(file.parentFile.toPath())
        file.writeBytes(byteArray)
    }

    /**
     * 计算文件在输出目录中的相对路径
     */
    private fun getRelativePathFromOriginPath(filePath: String): Path {
        val targetFilePath = Path.of(filePath).toAbsolutePath().normalize()
        return targetFilePath.relativeTo(originFilePath)
    }
}