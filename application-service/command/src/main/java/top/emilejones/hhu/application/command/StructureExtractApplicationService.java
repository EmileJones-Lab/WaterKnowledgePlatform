package top.emilejones.hhu.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.common.exception.AppException;
import top.emilejones.hhu.common.exception.InternalAppException;
import top.emilejones.hhu.common.exception.NotFoundException;
import top.emilejones.hhu.common.util.MD5Utils;
import top.emilejones.hhu.domain.pipeline.gateway.StructureExtractionGateway;

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
     * 对 Markdown 文件进行结构提取。
     * <p>
     * 将 Markdown 文本解析为图数据库中的树形结构节点，并持久化存储。
     * 若该文件已处理过，则会跳过。
     *
     * @param filePath Markdown 文件的本地路径（支持相对或绝对路径）
     * @throws IllegalArgumentException 如果文件不存在
     * @throws RuntimeException 如果提取过程或 IO 操作失败
     */
    public void extractTextStructure(String filePath) {
        Path path = validateAndResolvePath(filePath);
        String fileName = path.getFileName().toString();

        try {
            String sourceDocumentId = MD5Utils.calculateMD5(path);

            if (processRecordService.isAlreadyProcessed(sourceDocumentId)) {
                logger.info("文件 [{}] 结构已提取，跳过结构提取步骤。", fileName);
                return;
            }

            byte[] markdownBytes = Files.readAllBytes(path);
            logger.info("文件 [{}] 尚未提取结构，正在调用结构提取网关...", fileName);
            String fileNodeId = structureExtractionGateway.extract(markdownBytes, sourceDocumentId).getOrThrow();
            logger.info("结构提取成功，生成的根节点 ID 为: {}", fileNodeId);

            processRecordService.recordExtraction(sourceDocumentId, fileName, fileNodeId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("处理 Markdown 文件时发生错误: {}", e.getMessage(), e);
            throw new InternalAppException("结构提取任务执行失败");
        }
    }

    /**
     * 对 Markdown 文件进行摘要提取。
     * <p>
     * 调用摘要提取网关为已存在结构树节点生成摘要内容，并更新处理记录。
     * 若该文件摘要已生成过，则会跳过。
     *
     * @param filePath Markdown 文件的本地路径（支持相对或绝对路径）
     * @throws IllegalArgumentException 如果文件不存在
     * @throws RuntimeException 如果摘要提取过程失败
     */
    public void extractSummary(String filePath) {
        Path path = validateAndResolvePath(filePath);
        String fileName = path.getFileName().toString();

        try {
            String sourceDocumentId = MD5Utils.calculateMD5(path);

            if (processRecordService.isSummaryProcessed(sourceDocumentId)) {
                logger.info("文件 [{}] 摘要已提取过，跳过摘要生成步骤。", fileName);
                return;
            }

            logger.info("文件 [{}] 尚未提取摘要，正在调用摘要提取网关...", fileName);
            structureExtractionGateway.summary(sourceDocumentId).getOrThrow();

            processRecordService.updateSummaryStatus(sourceDocumentId, true);
            logger.info("摘要提取成功完成并已更新本地记录。");
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("处理 Markdown 文件时发生错误: {}", e.getMessage(), e);
            throw new InternalAppException("摘要提取任务执行失败");
        }
    }

    private Path validateAndResolvePath(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.error("文件不存在: {}", filePath);
            throw new NotFoundException("文件不存在: " + filePath);
        }
        return path;
    }
}
