package top.emilejones.hhu.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.common.util.MD5Utils;
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class StructureExtractApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(StructureExtractApplicationService.class);
    private final StructureExtractionGateway structureExtractionGateway;

    public StructureExtractApplicationService(StructureExtractionGateway structureExtractionGateway) {
        this.structureExtractionGateway = structureExtractionGateway;
    }

    /**
     * 调用已经存在的工具，将markdown文件转换为图结构并存储到图数据库中。
     * 需要打印必要的日志，让用户明白执行到了哪里。
     * sourceDocumentId使用给定文件的内容的MD5字符串。
     * 参考方法 top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway.extract
     *
     * 如果任务成功，则在当前目录下创建一个csv文件，在这个文件中追加这条数据的信息，例如：
     * sourceDocumentId, fileName
     * @param filePath markdown文件的相对路径或者绝对路径
     */
    public void extractStructure(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.error("文件不存在: {}", filePath);
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        String fileName = path.getFileName().toString();
        logger.info("开始处理 Markdown 文件: {}", fileName);

        try {
            // 1. 计算文件内容的 MD5 作为 sourceDocumentId
            String sourceDocumentId = MD5Utils.calculateMD5(path);
            logger.info("文件 MD5 计算完成: {}", sourceDocumentId);

            // 2. 调用结构提取网关
            try (InputStream inputStream = Files.newInputStream(path)) {
                logger.info("正在调用结构提取网关进行解析与存储...");
                String fileNodeId = structureExtractionGateway.extract(inputStream, sourceDocumentId);
                logger.info("结构提取成功完成，生成的根节点 ID 为: {}", fileNodeId);

                // 3. 将任务成功信息记录到 CSV 文件
                recordSuccess(sourceDocumentId, fileName);
                logger.info("任务成功信息已追加到 CSV 文件: structure_extraction_results.csv");
            }

        } catch (Exception e) {
            logger.error("处理 Markdown 文件时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("结构提取任务执行失败", e);
        }
    }

    private void recordSuccess(String sourceDocumentId, String fileName) throws IOException {
        String csvFileName = "structure_extraction_results.csv";
        Path csvPath = Paths.get(csvFileName);
        boolean exists = Files.exists(csvPath);

        try (BufferedWriter writer = Files.newBufferedWriter(csvPath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!exists) {
                writer.write("sourceDocumentId,fileName");
                writer.newLine();
            }
            writer.write(String.format("%s,%s", sourceDocumentId, fileName));
            writer.newLine();
        }
    }
}
