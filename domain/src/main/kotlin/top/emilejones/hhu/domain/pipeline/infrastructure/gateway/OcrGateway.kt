package top.emilejones.hhu.domain.pipeline.infrastructure.gateway

import java.io.InputStream

/**
 * OCR 外部能力抽象。
 * @author EmileJones
 */
interface OcrGateway {
    /**
     * 调用 MinerU 执行 OCR，返回处理后的流。
     */
    fun minerU(input: InputStream): InputStream
}
