package top.emilejones.hhu.preprocessing.handler.structure

import top.emilejones.hhu.preprocessing.handler.MarkdownFileHandler

/**
 * 主要用来修复遵循`1.1.1`这样格式的markdown文档，这个class做了以下任务
 * 1. 将`# 1`变为`## 1`，将`# 1.1.1`变为`#### 1.1.1`，如此类推
 * 2. 将`# （1）`变为` （1）`
 */
class TitleLevelCorrector : MarkdownFileHandler {
    companion object {
        private val regex = "^#*\\s*\\d{1,2}(\\.\\d{1,2})*\\.?\\s*[^、\\d\\s]+".toRegex()
        private val numberRegex = "\\d{1,2}(\\.\\d{1,2})*".toRegex()
    }

    override fun handle(markdownText: String): String {
        return markdownText.lines()
            .map { it.trimIndent() }
            .filter { it.isNotBlank() }
            .map {
                var result: String = it
                if (it.contains(regex)) {
                    val level = numberRegex.find(it)!!.value.split(".").size + 1
                    result = "#".repeat(level) + " " + it.replace('#', ' ').trimIndent()
                }
                result
            }.joinToString(separator = "\n")
    }
}