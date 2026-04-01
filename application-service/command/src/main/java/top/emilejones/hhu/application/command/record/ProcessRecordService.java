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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 处理记录服务。
 * 负责维护本地 CSV 文件，记录文件的结构提取和向量化状态。
 */
@Service
public class ProcessRecordService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRecordService.class);
    private static final String CSV_FILE_NAME = "record.csv";
    private static final String HEADER = "sourceDocumentId,fileName,isEmbedding";

    @PostConstruct
    public void init() {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        if (!Files.exists(csvPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.CREATE)) {
                writer.write(HEADER);
                writer.newLine();
                logger.info("已创建记录文件: {}", CSV_FILE_NAME);
            } catch (IOException e) {
                logger.error("无法创建记录文件 [{}]: {}", CSV_FILE_NAME, e.getMessage());
            }
        }
    }

    /**
     * 检查指定的 sourceDocumentId 是否已经处理过。
     *
     * @param sourceDocumentId 文档唯一标识
     * @return 如果记录已存在则返回 true
     */
    public boolean isAlreadyProcessed(String sourceDocumentId) {
        return findRecord(sourceDocumentId).isPresent();
    }

    /**
     * 记录结构提取成功。
     *
     * @param sourceDocumentId 文档唯一标识
     * @param fileName         文件名
     */
    public synchronized void recordExtraction(String sourceDocumentId, String fileName) {
        if (isAlreadyProcessed(sourceDocumentId)) {
            return;
        }
        Path csvPath = Paths.get(CSV_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.APPEND)) {
            writer.write(String.format("%s,%s,false", sourceDocumentId, fileName));
            writer.newLine();
        } catch (IOException e) {
            logger.error("记录提取结果失败: {}", e.getMessage());
        }
    }

    /**
     * 更新向量化状态。
     *
     * @param sourceDocumentId 文档唯一标识
     * @param isEmbedding      向量化状态
     */
    public synchronized void updateEmbeddingStatus(String sourceDocumentId, boolean isEmbedding) {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try {
            List<String> allLines = Files.readAllLines(csvPath);
            for (String line : allLines) {
                if (line.startsWith(sourceDocumentId + ",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        lines.add(String.format("%s,%s,%b", parts[0], parts[1], isEmbedding));
                        found = true;
                        continue;
                    }
                }
                lines.add(line);
            }

            if (found) {
                Files.write(csvPath, lines, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            logger.error("更新向量化状态失败: {}", e.getMessage());
        }
    }

    private Optional<String> findRecord(String sourceDocumentId) {
        Path csvPath = Paths.get(CSV_FILE_NAME);
        if (!Files.exists(csvPath)) {
            return Optional.empty();
        }

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            reader.readLine(); // 跳过表头
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(sourceDocumentId + ",")) {
                    return Optional.of(line);
                }
            }
        } catch (IOException e) {
            logger.warn("读取记录文件失败: {}", e.getMessage());
        }
        return Optional.empty();
    }
}
