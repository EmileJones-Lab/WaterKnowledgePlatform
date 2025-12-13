package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.utils.DtoConverter;
import top.emilejones.hhu.domain.knowledge.event.KnowledgeDocumentAddedToCatalogEvent;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.TextNode;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PipeLineApplicationService {
    private final ApplicationEventPublisher publisher;
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final EmbeddingGateway embeddingGateway;
    private final NodeRepository nodeRepository;

    /**
     * 构造函数。
     *
     * @param publisher                            Spring事件发布器。
     * @param structureExtractionMissionRepository 结构提取任务仓储。
     * @param embeddingMissionRepository           向量化任务仓储。
     * @param embeddingGateway                     向量化网关。
     * @param nodeRepository                       节点仓储。
     */
    public PipeLineApplicationService(
            ApplicationEventPublisher publisher,
            StructureExtractionMissionRepository structureExtractionMissionRepository,
            EmbeddingMissionRepository embeddingMissionRepository,
            EmbeddingGateway embeddingGateway,
            NodeRepository nodeRepository
    ) {
        this.publisher = publisher;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.nodeRepository = nodeRepository;
    }

    /**
     * 删除指定的结构提取任务。
     *
     * @param documentSplittingMissionId 结构提取任务的唯一标识。
     */
    public void deleteExtractStructureMission(String documentSplittingMissionId) {

    }

    /**
     * 批量开启结构提取任务。
     * 如果之前没有开启过OCR任务，则自动开启一个OCR任务。
     *
     * @param sourceDocumentIdList 文件的唯一ID列表。
     * @return 这批结构提取任务的详细信息列表。
     */
    public List<DocumentSplittingMissionDTO> startStructureExtractionMission(List<String> sourceDocumentIdList) {
        List<StructureExtractionMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateStructureExtractionMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions.stream()
                .map(DtoConverter::toDocumentSplittingMissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量开启层次结构向量化任务。
     * 如果此文件没有开启过OCR任务和结构提取任务，此接口会自动按顺序开启上述任务。
     *
     * @param sourceDocumentIdList 文件的唯一ID列表。
     * @return 这批向量化任务的详细信息列表。
     */
    public List<EmbeddingMissionDTO> startEmbeddingMission(List<String> sourceDocumentIdList) {
        List<EmbeddingMission> missions = sourceDocumentIdList.stream()
                .map(this::findExistingOrCreateEmbeddingMission)
                .toList();
        missions.forEach(mission -> mission.pushEvents().forEach(publisher::publishEvent));
        return missions.stream()
                .map(DtoConverter::toEmbeddingMissionDTO)
                .collect(Collectors.toList());
    }

    private StructureExtractionMission findExistingOrCreateStructureExtractionMission(String sourceDocumentId) {
        return structureExtractionMissionRepository.findBySourceDocumentId(sourceDocumentId).stream()
                .filter(StructureExtractionMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    StructureExtractionMission mission = StructureExtractionMission.Companion.create(
                            UUID.randomUUID().toString(),
                            sourceDocumentId
                    );
                    mission.preparedToExecution();
                    return mission;
                });
    }

    private EmbeddingMission findExistingOrCreateEmbeddingMission(String sourceDocumentId) {
        return embeddingMissionRepository.findBySourceDocumentId(sourceDocumentId).stream()
                .filter(EmbeddingMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    EmbeddingMission mission = EmbeddingMission.Companion.create(
                            UUID.randomUUID().toString(),
                            sourceDocumentId
                    );
                    mission.preparedToExecution();
                    return mission;
                });
    }

    /**
     * 监听 KnowledgeDocumentAddedToCatalogEvent 事件，负责将向量化数据存入 Milvus。
     *
     * @param event 添加知识文档到知识库目录的事件。
     */
    @Async("domainEventExecutor")
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handleKnowledgeDocumentAddedToCatalogEvent(KnowledgeDocumentAddedToCatalogEvent event) {
        String embeddingMissionId = event.getKnowledgeDocument().getEmbeddingMissionId();
        String milvusCollectionName = event.getKnowledgeCatalog().getMilvusCollectionName();
        
        try {
            // 2. 从 embeddingMissionRepository 查找对应的 EmbeddingMission
            EmbeddingMission mission = embeddingMissionRepository.findById(embeddingMissionId);
            if (mission == null) {
                throw new RuntimeException("EmbeddingMission not found for ID: " + embeddingMissionId);
            }

            // 3. 从 nodeRepository 获取与 EmbeddingMission 关联的 TextNodes
            String fileNodeId = mission.getFileNodeId();
            if (fileNodeId == null) {
                throw new RuntimeException("EmbeddingMission has no fileNodeId associated. Mission ID: " + embeddingMissionId);
            }
            List<TextNode> textNodes = nodeRepository.findTextNodeListByFileNodeId(fileNodeId);

            if (textNodes.isEmpty()) {
                return;
            }

            // 4. 调用 embeddingGateway.saveTextNodeToVectorDatabase 将 TextNodes 存入 Milvus (副作用)
            embeddingGateway.saveTextNodeToVectorDatabase(textNodes, milvusCollectionName);

        } catch (Exception e) {
            // 5. 如果失败，仅打印日志
            e.printStackTrace();
        }
    }
}


