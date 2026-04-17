package top.emilejones.hhu.application.command;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.common.util.MD5Utils;
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.repository.FileNodeVectorRepository;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository;
import top.emilejones.hhu.domain.result.FileNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 向量化应用服务。
 * 负责将指定文件中的内容进行向量化处理，并将结果（文本节点与文件节点）分别存储到不同的向量集合中。
 */
@Service
public class EmbeddingApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingApplicationService.class);
    
    /** 文本节点集合名称 */
    public static final String COLLECTION_NAME = "water_knowledge_platform";
    /** 文件节点集合名称 */
    public static final String FILE_COLLECTION_NAME = "water_knowledge_platform_file";

    private final EmbeddingGateway embeddingGateway;
    private final TextNodeVectorRepository textNodeVectorRepository;
    private final FileNodeVectorRepository fileNodeVectorRepository;
    private final NodeRepository nodeRepository;
    private final ProcessRecordService processRecordService;

    public EmbeddingApplicationService(EmbeddingGateway embeddingGateway,
                                       TextNodeVectorRepository textNodeVectorRepository,
                                       FileNodeVectorRepository fileNodeVectorRepository,
                                       NodeRepository nodeRepository,
                                       ProcessRecordService processRecordService) {
        this.embeddingGateway = embeddingGateway;
        this.textNodeVectorRepository = textNodeVectorRepository;
        this.fileNodeVectorRepository = fileNodeVectorRepository;
        this.nodeRepository = nodeRepository;
        this.processRecordService = processRecordService;
    }

    /**
     * 初始化方法，在 Spring Bean 创建后确保向量数据库的两个 Collection 已创建。
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("正在初始化文本节点向量集合: {}", COLLECTION_NAME);
            textNodeVectorRepository.createCollection(COLLECTION_NAME).getOrThrow();
            
            logger.info("正在初始化文件节点向量集合: {}", FILE_COLLECTION_NAME);
            fileNodeVectorRepository.createCollection(FILE_COLLECTION_NAME).getOrThrow();
            
            logger.info("向量数据库集合初始化完成。");
        } catch (Exception e) {
            logger.error("初始化向量数据库集合失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 将文件内容进行向量化并分别存入文本节点和文件节点向量库。
     *
     * @param filePath 文件路径
     */
    public void embed(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.error("文件不存在: {}", filePath);
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }

        try {
            // 1. 获取 sourceDocumentId
            String sourceDocumentId = MD5Utils.calculateMD5(path);
            logger.info("开始处理文件向量化，sourceDocumentId: {}", sourceDocumentId);

            // 2. 从本地记录中获取 fileNodeId
            String fileNodeId = processRecordService.getFileNodeId(sourceDocumentId)
                    .orElseThrow(() -> new IllegalStateException("未找到文件 [" + filePath + "] 的提取记录，请先执行结构提取。"));

            // 3. 调用新的 embed 方法进行完整向量化（内部会处理 TextNode 和 FileNode 的向量化并更新 Neo4j）
            embeddingGateway.embed(fileNodeId).getOrThrow();

            // 4. 从仓储中获取更新后的 FileNode
            FileNode fileNode = nodeRepository.findFileNodeByFileNodeId(fileNodeId)
                    .orElseThrow(() -> new NoSuchElementException("未找到 fileNodeId 为 [" + fileNodeId + "] 的文件节点"));

            // 5. 将文本节点数据同步到向量数据库
            textNodeVectorRepository.saveTextNodeToVectorDatabase(List.of(fileNodeId), COLLECTION_NAME).getOrThrow();

            // 6. 将文件节点数据同步到专属的向量数据库集合
            fileNodeVectorRepository.saveFileNodeToVectorDatabase(List.of(fileNode), FILE_COLLECTION_NAME).getOrThrow();

            // 7. 更新本地记录的向量化状态
            processRecordService.updateEmbeddingStatus(sourceDocumentId, true);

            logger.info("文件 [{}] (ID: {}) 向量化及双库同步任务成功完成。", filePath, sourceDocumentId);

        } catch (Exception e) {
            logger.error("文件向量化处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量化任务执行失败", e);
        }
    }
}
