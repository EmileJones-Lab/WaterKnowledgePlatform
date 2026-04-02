package top.emilejones.hhu.application.command;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.common.util.MD5Utils;
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.result.TextNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 向量化应用服务。
 * 负责将指定文件中的文本内容进行向量化处理，并将结果存储到向量数据库中。
 */
@Service
public class EmbeddingApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingApplicationService.class);
    public static final String COLLECTION_NAME = "water_knowledge_platform";

    private final EmbeddingGateway embeddingGateway;
    private final NodeRepository nodeRepository;
    private final ProcessRecordService processRecordService;

    public EmbeddingApplicationService(EmbeddingGateway embeddingGateway,
                                     NodeRepository nodeRepository,
                                     ProcessRecordService processRecordService) {
        this.embeddingGateway = embeddingGateway;
        this.nodeRepository = nodeRepository;
        this.processRecordService = processRecordService;
    }

    /**
     * 初始化方法，在 Spring Bean 创建后确保向量数据库的 Collection 已创建。
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("正在初始化向量数据库集合: {}", COLLECTION_NAME);
            embeddingGateway.createCollection(COLLECTION_NAME);
            logger.info("向量数据库集合初始化完成。");
        } catch (Exception e) {
            logger.error("初始化向量数据库集合失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 将文件内容进行向量化并存入向量数据库。
     * <p>
     * 处理逻辑如下：
     * 1. 校验文件是否存在。
     * 2. 计算文件的 MD5 值作为 sourceDocumentId。
     * 3. 从仓储中获取该文件对应的所有文本节点。
     * 4. 筛选出尚未进行向量化的节点。
     * 5. 调用向量化网关获取文本的向量。
     * 6. 更新节点向量信息并保存回仓储。
     * 7. 将向量化后的节点存入向量数据库（Milvus）。
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

            // 3. 从仓储中查找该文件下的文本节点
            List<TextNode> allNodes = nodeRepository.findTextNodeListByFileNodeId(fileNodeId);
            if (allNodes.isEmpty()) {
                logger.warn("未找到文件节点 [{}] 对应的文本节点。", fileNodeId);
                return;
            }

            // 4. 筛选未向量化的节点
            List<TextNode> nodesToEmbed = allNodes.stream()
                    .filter(node -> !node.isEmbedded())
                    .collect(Collectors.toList());

            if (nodesToEmbed.isEmpty()) {
                logger.info("文件 [{}] (ID: {}) 的所有文本节点均已向量化，跳过处理。", filePath, sourceDocumentId);
                return;
            }

            logger.info("准备对 {} 个文本节点进行向量化...", nodesToEmbed.size());

            // 4. 获取文本列表进行批量向量化
            List<String> texts = nodesToEmbed.stream()
                    .map(TextNode::getSummary)
                    .collect(Collectors.toList());

            List<List<Float>> vectors = embeddingGateway.embed(texts);

            if (vectors.size() != nodesToEmbed.size()) {
                throw new RuntimeException("向量化结果数量与输入文本节点数量不匹配");
            }

            // 5. 更新节点并保存回仓储
            for (int i = 0; i < nodesToEmbed.size(); i++) {
                TextNode node = nodesToEmbed.get(i);
                List<Float> vector = vectors.get(i);
                node.saveVector(vector);
                nodeRepository.saveTextNode(node);
            }

            // 6. 保存到向量数据库
            embeddingGateway.saveTextNodeToVectorDatabase(nodesToEmbed, COLLECTION_NAME);

            // 7. 更新本地记录的向量化状态
            processRecordService.updateEmbeddingStatus(sourceDocumentId, true);

            logger.info("文件 [{}] (ID: {}) 向量化任务成功完成。", filePath, sourceDocumentId);

        } catch (Exception e) {
            logger.error("文件向量化处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量化任务执行失败", e);
        }
    }
}
