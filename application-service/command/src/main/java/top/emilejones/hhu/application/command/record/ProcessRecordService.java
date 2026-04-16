package top.emilejones.hhu.application.command.record;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 处理记录服务。
 * 负责维护本地 CSV 文件，记录文件的结构提取和向量化状态。
 */
@Service
public class ProcessRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRecordService.class);
    private static final String CSV_FILE_NAME = "record.csv";
    private static final String HEADER = "sourceDocumentId,fileName,fileNodeId,isEmbedding,isSummary";

    // 使用 LinkedHashMap 保持记录的插入顺序，key 为 sourceDocumentId
    private final Map<String, ProcessRecord> records = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        // 如果文件不存在，则创建并写入表头
        if (!Files.exists(csvPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.CREATE)) {
                writer.write(HEADER);
                writer.newLine();
                logger.info("已创建记录文件: {}", CSV_FILE_NAME);
            } catch (IOException e) {
                logger.error("无法创建记录文件 [{}]: {}", CSV_FILE_NAME, e.getMessage());
            }
        }
        // 初始化时加载现有记录到内存
        loadRecords();
    }

    /**
     * 从 CSV 文件加载所有记录到内存 Map。
     */
    private synchronized void loadRecords() {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        if (!Files.exists(csvPath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line = reader.readLine(); // 跳过表头
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    records.put(parts[0], new ProcessRecord(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3]), Boolean.parseBoolean(parts[4])));
                }
            }
            logger.info("已从文件加载 {} 条处理记录", records.size());
        } catch (IOException e) {
            logger.error("加载记录文件失败: {}", e.getMessage());
        }
    }

    /**
     * 检查指定的 sourceDocumentId 是否已经处理过。
     *
     * @param sourceDocumentId 文档唯一标识
     * @return 如果记录已存在则返回 true
     */
    public synchronized boolean isAlreadyProcessed(String sourceDocumentId) {
        return records.containsKey(sourceDocumentId);
    }

    /**
     * 记录结构提取成功。
     *
     * @param sourceDocumentId 文档唯一标识
     * @param fileName         文件名
     * @param fileNodeId       图数据库中的文件节点 ID
     */
    public synchronized void recordExtraction(String sourceDocumentId, String fileName, String fileNodeId) {
        if (records.containsKey(sourceDocumentId)) {
            return;
        }

        ProcessRecord record = new ProcessRecord(sourceDocumentId, fileName, fileNodeId, false, false);
        // 先添加到内存
        records.put(sourceDocumentId, record);

        // 再追加到文件
        Path csvPath = Paths.get(CSV_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.APPEND)) {
            writer.write(record.toCsvLine());
            writer.newLine();
        } catch (IOException e) {
            logger.error("追加记录提取结果失败: {}", e.getMessage());
        }
    }

    /**
     * 更新向量化状态。
     *
     * @param sourceDocumentId 文档唯一标识
     * @param isEmbedding      向量化状态
     */
    public synchronized void updateEmbeddingStatus(String sourceDocumentId, boolean isEmbedding) {
        ProcessRecord record = records.get(sourceDocumentId);
        if (record != null) {
            // 更新内存记录
            records.put(sourceDocumentId, new ProcessRecord(record.sourceDocumentId(), record.fileName(), record.fileNodeId(), isEmbedding, record.isSummary()));
            // 同步回文件
            syncToFile();
        }
    }

    /**
     * 更新摘要生成状态。
     *
     * @param sourceDocumentId 文档唯一标识
     * @param isSummary        摘要生成状态
     */
    public synchronized void updateSummaryStatus(String sourceDocumentId, boolean isSummary) {
        ProcessRecord record = records.get(sourceDocumentId);
        if (record != null) {
            // 更新内存记录
            records.put(sourceDocumentId, new ProcessRecord(record.sourceDocumentId(), record.fileName(), record.fileNodeId(), record.isEmbedding(), isSummary));
            // 同步回文件
            syncToFile();
        }
    }

    /**
     * 将内存中的所有记录全量同步回 CSV 文件。
     */
    private synchronized void syncToFile() {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(HEADER);
            writer.newLine();
            for (ProcessRecord record : records.values()) {
                writer.write(record.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("同步记录到文件失败: {}", e.getMessage());
        }
    }

    /**
     * 根据 sourceDocumentId 获取记录中的 fileNodeId。
     *
     * @param sourceDocumentId 文档唯一标识
     * @return fileNodeId 的 Optional 封装
     */
    public synchronized Optional<String> getFileNodeId(String sourceDocumentId) {
        ProcessRecord record = records.get(sourceDocumentId);
        return record != null ? Optional.of(record.fileNodeId()) : Optional.empty();
    }

    /**
     * 根据 fileNodeId 获取对应的文件名。
     *
     * @param fileNodeId 文件节点 ID
     * @return 文件名字符串，未找到则返回 "未知"
     */
    public synchronized String getFileNameByFileNodeId(String fileNodeId) {
        return records.values().stream()
                .filter(record -> fileNodeId.equals(record.fileNodeId()))
                .map(ProcessRecord::fileName)
                .findFirst()
                .orElse("未知");
    }

    /**
     * 处理记录的数据载体。
     */
    private record ProcessRecord(String sourceDocumentId, String fileName, String fileNodeId, boolean isEmbedding, boolean isSummary) {
        public String toCsvLine() {
            return String.format("%s,%s,%s,%b,%b", sourceDocumentId, fileName, fileNodeId, isEmbedding, isSummary);
        }
    }
}
