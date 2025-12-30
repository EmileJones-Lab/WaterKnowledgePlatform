package top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto

/**
 * MinerU 提取的图片。
 * data 中存放还原后的二进制数据，不保留 Base64 字符串。
 * relativePath 格式类似 `images/test.png`（不包含 `./` 前缀）。
 */
data class MinerUImage(
    val imageName: String,
    val contentType: String?,
    val data: ByteArray,
    val relativePath: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinerUImage

        if (imageName != other.imageName) return false
        if (contentType != other.contentType) return false
        if (!data.contentEquals(other.data)) return false
        if (relativePath != other.relativePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageName.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        result = 31 * result + relativePath.hashCode()
        return result
    }
}
