package top.emilejones.hhu.application.command.dto;

import java.util.List;

/**
 * OCR 结果响应 DTO。
 */
public record MinerUMarkdownResponse(
    String markdownContent,
    List<MinerUImageResponse> images
) {
}
