package top.emilejones.hhu.domain.pipeline.gateway

import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile
import java.io.InputStream

/**
 * OCR 外部能力抽象，封装对 MinerU 的调用。
 */
interface OcrGateway {
    /**
     * 调用 MinerU 执行 OCR 并返回处理结果。
     *
     * 约定：
     * - 入参为原始文件内容流（图片、PDF 等），调用方负责管理流的生命周期。
     * - 返回包含规范化后的 Markdown 文本与图片二进制数据的结构体；调用方可自行持久化图片，`relativePath` 形如 `images/test.png`（无 `./` 前缀）。
     * - 实现应保证调用失败时抛出明确的异常，便于上层感知并告警。
     *
     * @param input 待识别的二进制输入流
     * @return 经 MinerU 识别、转换后的 Markdown 及图片数据
     */
    fun minerU(input: InputStream): MinerUMarkdownFile
}
