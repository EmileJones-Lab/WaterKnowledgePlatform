package top.emilejones.hhu.common


object FileUtils {

    /**
     * 判断二进制内容是否为 PDF 格式。
     * 通过检查文件头标识（Magic Number）: %PDF- (十六进制: 25 50 44 46 2D)
     *
     * @param content 文件二进制内容
     * @return 如果是 PDF 返回 true，否则返回 false
     */
    fun checkPdf(content: ByteArray): Boolean {
        if (content.size < 5) {
            return false
        }

        return content[0].toInt() == 0x25 && // %
                content[1].toInt() == 0x50 && // P
                content[2].toInt() == 0x44 && // D
                content[3].toInt() == 0x46 && // F
                content[4].toInt() == 0x2D    // -
    }
}