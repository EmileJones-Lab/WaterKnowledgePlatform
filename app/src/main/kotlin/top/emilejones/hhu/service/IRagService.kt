package top.emilejones.hhu.service

import java.nio.file.Path

/**
 * 文本预处理的接口定义
 * @author EmileJones
 */
interface IRagService {
    /**
     * 将Markdown文件进行预处理后存入所有相关的数据库中
     * @param filePath 文件路径
     */
    suspend fun saveMarkdownFileToAllDatabase(filePath: Path)
}