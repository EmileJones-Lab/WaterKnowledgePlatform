package top.emilejones.hhu.application.command.dto;

/**
 * MinerU 提取的图片响应 DTO。
 */
public record MinerUImageResponse(
    String imageName,
    String contentType,
    byte[] data,
    String relativePath
) {
}
