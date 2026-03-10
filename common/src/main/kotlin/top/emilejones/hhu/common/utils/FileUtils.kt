package top.emilejones.hhu.common.utils

import java.io.BufferedInputStream
import java.io.InputStream

object FileUtils {

    /**
     * 判断输入流是否为 PDF 格式
     * 通过检查文件头标识（Magic Number）: %PDF- (十六进制: 25 50 44 46 2D)
     *
     * @param inputStream 输入流
     * @return 如果是 PDF 返回 true，否则返回 false
     */
    fun isPdf(inputStream: InputStream): Boolean {
        val bis = if (inputStream.markSupported()) inputStream else BufferedInputStream(inputStream)
        
        // PDF 文件头通常是 5 个字节: %PDF-
        val headerLength = 5
        bis.mark(headerLength)
        
        val header = ByteArray(headerLength)
        val bytesRead = bis.read(header)
        
        // 即使读取失败或不匹配，也要尝试重置流，以便外部继续使用（如果外部传入的是可重复读取的流）
        // 注意：如果是普通 InputStream，重置可能会失败，但在 markSupported 的情况下通常是安全的
        try {
            bis.reset()
        } catch (e: Exception) {
            // 忽略重置失败的情况
        }

        if (bytesRead != headerLength) {
            return false
        }

        // 检查是否以 %PDF- 开头
        return header[0].toInt() == 0x25 && // %
               header[1].toInt() == 0x50 && // P
               header[2].toInt() == 0x44 && // D
               header[3].toInt() == 0x46 && // F
               header[4].toInt() == 0x2D    // -
    }
}
