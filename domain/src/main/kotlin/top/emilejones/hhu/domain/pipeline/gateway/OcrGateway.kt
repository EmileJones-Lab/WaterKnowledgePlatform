package top.emilejones.hhu.domain.pipeline.gateway

import top.emilejones.hhu.common.Result
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile

/**
 * OCR 外部能力抽象，封装对 MinerU 的调用。
 */
interface OcrGateway {
    /**
     * 调用 MinerU 执行 OCR 并返回处理结果。
     *
     * 约定：
     * - 入参为原始文件二进制内容（图片、PDF 等）。
     * - 返回包含规范化后的 Markdown 文本与图片二进制数据的结构体；调用方可自行持久化图片，`relativePath` 形如 `images/test.png`（无 `./` 前缀）。
     * - 封装在 Result 中，成功时包含识别结果，失败时包含明确的异常。
     *
     * @param input 待识别的二进制字节数组
     * @return 识别结果
     */
    fun minerU(input: ByteArray): Result<MinerUMarkdownFile>
}
