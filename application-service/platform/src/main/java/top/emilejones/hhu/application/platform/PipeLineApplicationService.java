package top.emilejones.hhu.application.platform;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import top.emilejones.hhu.application.platform.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.platform.statemachine.PipelineContext;
import top.emilejones.hhu.application.platform.statemachine.PipelineEvent;
import top.emilejones.hhu.application.platform.statemachine.PipelineState;
import top.emilejones.hhu.application.platform.utils.DtoConverter;
import top.emilejones.hhu.domain.document.repository.SourceDocumentRepository;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocument;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeDocumentRepository;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.repository.*;
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.result.MissionStatus;
import top.emilejones.hhu.domain.result.TextNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PipeLineApplicationService {
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final EmbeddingGateway embeddingGateway;
    private final TextNodeVectorRepository textNodeVectorRepository;
    private final NodeRepository nodeRepository;
    private final OcrMissionRepository ocrMissionRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;
    private final ProcessedDocumentRepository processedDocumentRepository;
    private final SourceDocumentRepository sourceDocumentRepository;
    private final StateMachineFactory<PipelineState, PipelineEvent> stateMachineFactory;

    public PipeLineApplicationService(StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingMissionRepository embeddingMissionRepository, EmbeddingGateway embeddingGateway, TextNodeVectorRepository textNodeVectorRepository, NodeRepository nodeRepository, OcrMissionRepository ocrMissionRepository, KnowledgeDocumentRepository knowledgeDocumentRepository, KnowledgeCatalogRepository knowledgeCatalogRepository, ProcessedDocumentRepository processedDocumentRepository, SourceDocumentRepository sourceDocumentRepository, StateMachineFactory<PipelineState, PipelineEvent> stateMachineFactory) {
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.embeddingGateway = embeddingGateway;
        this.textNodeVectorRepository = textNodeVectorRepository;
        this.nodeRepository = nodeRepository;
        this.ocrMissionRepository = ocrMissionRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
        this.processedDocumentRepository = processedDocumentRepository;
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    /**
     * 根据文件唯一ID删除所有相关的任务以及任务成功生成的结果文件
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
                    textNodeVectorRepository.deleteTextNodeFromVectorDatabases(List.of(fileNodeId), knowledgeCatalog.getMilvusCollectionName()).getOrThrow();
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
                .map(id -> {
                    Optional<StructureExtractionMission> existing = structureExtractionMissionRepository.findBySourceDocumentId(id).stream()
                            .filter(mission -> mission.isSuccess() ||
                                    MissionStatus.RUNNING.equals(mission.getStatus()) ||
                                    MissionStatus.PENDING.equals(mission.getStatus()))
                            .findFirst();

                    StructureExtractionMission mission;
                    if (existing.isPresent()) {
                        mission = existing.get();
                    } else {
                        mission = StructureExtractionMission.Companion.create(id);
                        structureExtractionMissionRepository.save(mission);
                        startPipeline(id, PipelineState.STRUCTURE_EXTRACTION, mission.getId());
                    }
                    return mission;
                })
                .map(DtoConverter::toDocumentSplittingMissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 启动文档处理流水线。
     *
     * @param sourceDocumentId 源文件唯一标识 ID。
     * @param targetState      流水线预期的目标终点状态。
     * @param topMissionId     发起此次流水线的最顶层任务 ID。
     */
    private void startPipeline(String sourceDocumentId, PipelineState targetState, String topMissionId) {
        StateMachine<PipelineState, PipelineEvent> stateMachine = stateMachineFactory.getStateMachine();
        PipelineContext context = PipelineContext.builder()
                .sourceDocumentId(sourceDocumentId)
                .targetState(targetState)
                .currentTopMissionId(topMissionId)
                .build();
        stateMachine.getExtendedState().getVariables().put("context", context);
        stateMachine.startReactively()
                .thenMany(stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(PipelineEvent.TO_OCR).build())))
                .subscribe();
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
                .map(id -> {
                    Optional<EmbeddingMission> existing = embeddingMissionRepository.findBySourceDocumentId(id).stream()
                            .filter(mission -> mission.isSuccess() ||
                                    MissionStatus.RUNNING.equals(mission.getStatus()) ||
                                    MissionStatus.PENDING.equals(mission.getStatus()))
                            .findFirst();

                    EmbeddingMission mission;
                    if (existing.isPresent()) {
                        mission = existing.get();
                    } else {
                        mission = EmbeddingMission.Companion.create(id);
                        embeddingMissionRepository.save(mission);
                        startPipeline(id, PipelineState.EMBEDDING, mission.getId());
                    }
                    return mission;
                })
                .map(DtoConverter::toEmbeddingMissionDTO)
                .collect(Collectors.toList());
    }
}


