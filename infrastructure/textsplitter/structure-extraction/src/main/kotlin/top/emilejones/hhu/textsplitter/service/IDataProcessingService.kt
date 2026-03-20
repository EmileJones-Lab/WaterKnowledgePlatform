package top.emilejones.hhu.textsplitter.service

import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile
import java.io.InputStream

/**
 * 文本预处理的接口定义
 * @author EmileJones
 */
interface IDataProcessingService {

    /**
     * 将文件使用OCR的方式转换为markdown格式的文件
     * @param fileInputStream 文件内容
     *
     * @return ocr后的文件内容
     */
    fun ocrFileToMarkdownFile(
        fileInputStream: InputStream,
    ): Result<MinerUMarkdownFile>

    /**
     * 从指定Markdown文件中提取出文本结构，并将其存入neo4j中
     * @param inputStream 需要提取文件结构的id
     * @return neo4j中的FileNode的elementId
     */
    fun extractMarkdownStructure(fileId: String, inputStream: InputStream): Result<String>

    /**
     * 将指定的Markdown文件的文本结构片段向量化并保存
     * @param fileId 需要提取文件结构的id
     */
    fun embedTextNodes(fileId: String): Result<Unit>

    /**
     * 根据fileId去找到FileNode，将向量化后的TextNode存入到Milvus中
     * @param fileId 文件唯一Id
     * @param collectionName 需要插入到哪一个Collection中
     */
    fun saveTextNodeToMilvus(fileId: String, collectionName: String)
}
