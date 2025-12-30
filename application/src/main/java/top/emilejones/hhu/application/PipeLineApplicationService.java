package top.emilejones.hhu.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.utils.DtoConverter;
import top.emilejones.hhu.common.utils.Pair;
import top.emilejones.hhu.domain.document.infrastruction.SourceDocumentRepository;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.event.CreatedKnowledgeCatalogEvent;
import top.emilejones.hhu.domain.knowledge.event.KnowledgeDocumentAddedToCatalogEvent;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.TextNode;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.*;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;

import java.util.List;
import java.util.Optional;
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
    private final OcrMissionRepository ocrMissionRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;
    private final ProcessedDocumentRepository processedDocumentRepository;
    private final SourceDocumentRepository sourceDocumentRepository;

    public PipeLineApplicationService(ApplicationEventPublisher publisher, StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingMissionRepository embeddingMissionRepository, EmbeddingGateway embeddingGateway, NodeRepository nodeRepository, OcrMissionRepository ocrMissionRepository, KnowledgeDocumentRepository knowledgeDocumentRepository, KnowledgeCatalogRepository knowledgeCatalogRepository, ProcessedDocumentRepository processedDocumentRepository, SourceDocumentRepository sourceDocumentRepository) {
        this.publisher = publisher;
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.nodeRepository = nodeRepository;
        this.ocrMissionRepository = ocrMissionRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
        this.processedDocumentRepository = processedDocumentRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
    }

    /**
     * 删除指定的结构提取任务。
     *
     * @param fileId 文件唯一Id。
     */
    public void deleteMissions(String fileId) {
        // 找到OCR任务
        List<OcrMission> allOcrMission = ocrMissionRepository.findBySourceDocumentId(fileId);

        if (allOcrMission.isEmpty())
            throw new IllegalStateException("文件 [" + fileId + "] 不存在任务，不能执行删除操作");

        // 找到结构提取任务
        List<StructureExtractionMission> allSplitterMission = structureExtractionMissionRepository.findBySourceDocumentId(fileId);

        Optional<StructureExtractionMission> successSplitterMissionOptional = allSplitterMission.stream()
                .filter(mission ->
                        MissionStatus.SUCCESS.equals(mission.getStatus())
                ).findFirst();


        // 找到向量化任务
        List<EmbeddingMission> allEmbeddingMission = embeddingMissionRepository.findBySourceDocumentId(fileId);

        Optional<EmbeddingMission> successEmbeddingMissionOptional = allEmbeddingMission.stream()
                .filter(mission ->
                        MissionStatus.SUCCESS.equals(mission.getStatus())
                ).findFirst();

        // 删除OCR任务
        processedDocumentRepository.deleteBySourceDocumentId(fileId);
        allOcrMission.stream().map(OcrMission::getId).forEach(ocrMissionRepository::delete);

        if (successSplitterMissionOptional.isPresent()) {
            // 如果存在成功的结构提取任务则删除相关图数据
            StructureExtractionMission successSplitterMission = successSplitterMissionOptional.get();
            String fileNodeId = successSplitterMission.getSuccessResult().getFileNodeId();


            if (successEmbeddingMissionOptional.isPresent()) {
                // 删除向量化任务产生的相关知识文件
                EmbeddingMission successEmbeddingMission = successEmbeddingMissionOptional.get();
                KnowledgeDocument knowledgeDocument = knowledgeDocumentRepository.findByEmbeddingMissionId(successEmbeddingMission.getId());
                List<KnowledgeCatalog> knowledgeCatalogList = knowledgeDocumentRepository.findKnowledgeCatalogByKnowledgeDocumentId(knowledgeDocument.getId());
                List<TextNode> textNodeList = nodeRepository.findTextNodeListByFileNodeId(fileNodeId);
                List<String> textNodeIdList = textNodeList.stream().map(TextNode::getId).toList();
                // 将每个知识文件和知识库解绑
                knowledgeCatalogList.forEach(knowledgeCatalog -> {
                    knowledgeCatalogRepository.deleteKnowledgeDocumentFromKnowledgeCatalog(knowledgeCatalog.getId(), List.of(knowledgeDocument.getId()));
                    embeddingGateway.deleteTextNodeFromVectorDatabases(textNodeIdList, knowledgeCatalog.getMilvusCollectionName());
                });
                // 删除知识文件
                knowledgeDocumentRepository.delete(knowledgeDocument.getId());
            }

            // 删除结构提取任务产生的图结果
            nodeRepository.deleteAllNodeByFileNodeId(fileNodeId);
        }
        // 删除结构化任务记录
        allSplitterMission.stream().map(StructureExtractionMission::getId).forEach(structureExtractionMissionRepository::delete);
        // 删除向量化任务记录
        allEmbeddingMission.stream().map(EmbeddingMission::getId).forEach(embeddingMissionRepository::delete);
    }

    /**
     * 批量开启结构提取任务。
     * 如果之前没有开启过OCR任务，则自动开启一个OCR任务。
     *
     * @param sourceDocumentIdList 文件的唯一ID列表。
     * @return 这批结构提取任务的详细信息列表。
     */
    public List<DocumentSplittingMissionDTO> startStructureExtractionMission(List<String> sourceDocumentIdList) {
        // 判断这个sourceDocumentIdList中的文件是否都存在
        List<String> notExistFileIds = sourceDocumentIdList.stream()
                .filter(id -> sourceDocumentRepository.findSourceDocumentById(id).isEmpty())
                .toList();

        if (!notExistFileIds.isEmpty()) {
            String ids = notExistFileIds.stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException("不存在的文件，文件ID：" + ids);
        }

        return sourceDocumentIdList.stream()
                // 1. 查找已存在的有效任务
                .map(id -> {
                    Optional<StructureExtractionMission> existing = structureExtractionMissionRepository.findBySourceDocumentId(id).stream()
                            .filter(mission -> mission.isSuccess() ||
                                    MissionStatus.RUNNING.equals(mission.getStatus()) ||
                                    MissionStatus.PENDING.equals(mission.getStatus()))
                            .findFirst();
                    return new Pair<>(id, existing);
                })
                // 2. 如果不存在则创建新任务，并标记为新建
                .map(entry -> {
                    if (entry.getValue().isPresent()) {
                        return new Pair<>(entry.getValue().get(), false);
                    } else {
                        StructureExtractionMission newMission = StructureExtractionMission.Companion.create(
                                UUID.randomUUID().toString(),
                                entry.getKey()
                        );
                        return new Pair<>(newMission, true);
                    }
                })
                // 3. 发布新建任务事件
                .peek(entry -> {
                    if (entry.getValue()) {
                        publisher.publishEvent(entry.getKey());
                    }
                })
                // 4. 提取任务实体
                .map(Pair::getKey)
                // 5. 转换为DTO
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
        // 判断这个sourceDocumentIdList中的文件是否都存在
        List<String> notExistFileIds = sourceDocumentIdList.stream()
                .filter(id -> sourceDocumentRepository.findSourceDocumentById(id).isEmpty())
                .toList();

        if (!notExistFileIds.isEmpty()) {
            String ids = notExistFileIds.stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException("不存在的文件，文件ID：" + ids);
        }

        return sourceDocumentIdList.stream()
                // 1. 查找已存在的有效任务
                .map(id -> {
                    Optional<EmbeddingMission> existing = embeddingMissionRepository.findBySourceDocumentId(id).stream()
                            .filter(mission -> mission.isSuccess() ||
                                    MissionStatus.RUNNING.equals(mission.getStatus()) ||
                                    MissionStatus.PENDING.equals(mission.getStatus()))
                            .findFirst();
                    return new Pair<>(id, existing);
                })
                // 2. 如果不存在则创建新任务，并标记为新建
                .map(entry -> {
                    if (entry.getValue().isPresent()) {
                        return new Pair<>(entry.getValue().get(), false);
                    } else {
                        EmbeddingMission newMission = EmbeddingMission.Companion.create(
                                UUID.randomUUID().toString(),
                                entry.getKey()
                        );
                        return new Pair<>(newMission, true);
                    }
                })
                // 3. 发布新建任务事件
                .peek(entry -> {
                    if (entry.getValue()) {
                        publisher.publishEvent(entry.getKey());
                    }
                })
                // 4. 提取任务实体
                .map(Pair::getKey)
                // 5. 转换为DTO
                .map(DtoConverter::toEmbeddingMissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 监听 KnowledgeDocumentAddedToCatalogEvent 事件，负责将向量化数据存入 Milvus。
     *
     * @param event 添加知识文档到知识库目录的事件。
     */
    @EventListener
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

    @EventListener
    public void handleCreatedKnowledgeCatalogEvent(CreatedKnowledgeCatalogEvent event) {
        embeddingGateway.createCollection(event.getNewKnowledgeCatalog().getMilvusCollectionName());
    }
}


