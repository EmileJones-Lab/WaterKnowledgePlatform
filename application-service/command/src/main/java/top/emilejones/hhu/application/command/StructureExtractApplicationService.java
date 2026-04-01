package top.emilejones.hhu.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.common.util.MD5Utils;
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 结构提取应用服务。
 * 提供将 Markdown 文件解析并转换为图数据库中树形结构的能力，通常用于命令行工具或批量处理场景。
 */
@Service
public class StructureExtractApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(StructureExtractApplicationService.class);
    private final StructureExtractionGateway structureExtractionGateway;
    private final ProcessRecordService processRecordService;

    public StructureExtractApplicationService(StructureExtractionGateway structureExtractionGateway,
                                              ProcessRecordService processRecordService) {
        this.structureExtractionGateway = structureExtractionGateway;
        this.processRecordService = processRecordService;
    }

    /**
     * 执行 Markdown 文件结构提取任务。
     * <p>
     * 该方法会执行以下步骤：
     * 1. 校验文件是否存在。
     * 2. 计算文件内容的 MD5 值作为唯一标识 (sourceDocumentId)。
     * 3. 检查记录，判断该 MD5 是否已被处理过。
     * 4. 调用结构提取网关将文本转换为图节点并持久化到图数据库。
     * 5. 任务成功后，调用处理记录服务将结果记录到本地 CSV。
     *
     * @param filePath Markdown 文件的本地路径（支持相对或绝对路径）
     * @throws IllegalArgumentException 如果文件不存在
     * @throws RuntimeException 如果提取过程或 IO 操作失败
     */
    public void extractStructure(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.error("文件不存在: {}", filePath);
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        String fileName = path.getFileName().toString();

        try {
            // 1. 计算文件内容的 MD5 作为 sourceDocumentId
            String sourceDocumentId = MD5Utils.calculateMD5(path);

            // 2. 幂等性检查：判断是否已提取过
            if (processRecordService.isAlreadyProcessed(sourceDocumentId)) {
                logger.info("文件 [{}] (MD5: {}) 已被提取过，跳过处理。", fileName, sourceDocumentId);
                return;
            }

            logger.info("开始处理 Markdown 文件: {}", fileName);

            // 3. 调用结构提取网关
            try (InputStream inputStream = Files.newInputStream(path)) {
                logger.info("正在调用结构提取网关进行解析与存储...");
                String fileNodeId = structureExtractionGateway.extract(inputStream, sourceDocumentId);
                logger.info("结构提取成功完成，生成的根节点 ID 为: {}", fileNodeId);

                // 4. 记录提取结果到本地 CSV
                processRecordService.recordExtraction(sourceDocumentId, fileName, fileNodeId);
                logger.info("任务成功信息已记录到本地。");
            }

        } catch (Exception e) {
            logger.error("处理 Markdown 文件时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("结构提取任务执行失败", e);
        }
    }
}
