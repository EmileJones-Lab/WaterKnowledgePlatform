package top.emilejones.hhu.domain.pipeline.infrastructure.dto

/**
 * OCR 结果：Markdown 内容 + 图片资源。
 */
data class MinerUMarkdownFile(
    val markdownContent: String,
    val images: List<MinerUImage>
)
