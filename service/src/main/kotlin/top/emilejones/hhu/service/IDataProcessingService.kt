package top.emilejones.hhu.service

import java.io.InputStream
import java.nio.file.Path

/**
 * 文本预处理的接口定义
 * @author EmileJones
 */
interface IDataProcessingService {
    /**
     * 将Markdown文件进行预处理后存入所有相关的数据库中
     * @param filePath 文件路径
     */
    suspend fun saveMarkdownFileToAllDatabase(filePath: Path)

    /**
     * 将文件使用OCR的方式转换为markdown格式的文件
     * @param filename 文件名称
     * @param fileInputStream 文件内容
     * @param contentType 文件类型
     * @param catalogId 这个文件属于的目录Id
     *
     * @return ocr后的文件内容
     */
    fun ocrFileToMarkdownFile(
        filename: String,
        fileInputStream: InputStream,
        contentType: String,
        catalogId: String
    ): Result<InputStream>

    /**
     * 从指定Markdown文件中提取出文本结构，并将其存入neo4j中
     * @param fileId 需要提取文件结构的id
     * @return neo4j中的FileNode的elementId
     */
    fun extractMarkdownStructure(fileId: String): Result<String>

    /**
     * 将指定的Markdown文件的文本结构片段向量化并保存
     * @param fileId 需要提取文件结构的id
     * @return neo4j中的FileNode的elementId
     */
    fun embedMarkdownFileChunks(fileId: String)
}
