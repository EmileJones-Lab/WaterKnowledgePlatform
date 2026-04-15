package top.emilejones.hhu.application.command;

import org.springframework.stereotype.Service;
import top.emilejones.hhu.common.Result;
import top.emilejones.hhu.application.command.dto.MinerUImageResponse;
import top.emilejones.hhu.application.command.dto.MinerUMarkdownResponse;
import top.emilejones.hhu.domain.pipeline.gateway.OcrGateway;
import top.emilejones.hhu.domain.pipeline.gateway.dto.MinerUMarkdownFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OcrApplicationService {
    private final OcrGateway ocrGateway;

    public OcrApplicationService(OcrGateway ocrGateway) {
        this.ocrGateway = ocrGateway;
    }

    /**
     * 根据 URL 或本地文件路径提取结构化内容。
     *
     * @param source PDF 文件的 URL 或本地路径（支持相对路径）
     * @return 提取后的 Markdown 内容及相关图片
     */
    public MinerUMarkdownResponse extractStructure(String source) {
        try (InputStream inputStream = openStream(source);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

            // 检查 PDF 魔数 %PDF-
            bufferedInputStream.mark(4);
            byte[] header = new byte[4];
            int readBytes = bufferedInputStream.read(header);
            if (readBytes != 4 || !new String(header).startsWith("%PDF")) {
                throw new IllegalArgumentException("The file at the provided source is not a valid PDF file.");
            }
            bufferedInputStream.reset();

            // 调用 OCR 网关
            MinerUMarkdownFile minerUMarkdownFile = ocrGateway.minerU(bufferedInputStream).getOrThrow();

            // 映射到 Application 层 DTO
            return mapToResponse(minerUMarkdownFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch or process data from: " + source, e);
        }
    }

    private InputStream openStream(String source) throws IOException {
        if (source.contains("://")) {
            return URI.create(source).toURL().openStream();
        }
        return Files.newInputStream(Paths.get(source));
    }

    private MinerUMarkdownResponse mapToResponse(MinerUMarkdownFile minerUMarkdownFile) {
        List<MinerUImageResponse> images = minerUMarkdownFile.getImages().stream()
                .map(image -> new MinerUImageResponse(
                        image.getImageName(),
                        image.getContentType(),
                        image.getData(),
                        image.getRelativePath()
                ))
                .collect(Collectors.toList());

        return new MinerUMarkdownResponse(
                minerUMarkdownFile.getMarkdownContent(),
                images
        );
    }
}
