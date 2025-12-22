package top.emilejones.hhu.application.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.FileNode;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.TextNode;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.EmbeddingGateway;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.EmbeddingMissionRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.StructureExtractionMissionRepository;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmbeddingProcessor {
    private final StructureExtractionMissionRepository structureExtractionMissionRepository;
    private final EmbeddingMissionRepository embeddingMissionRepository;
    private final NodeRepository nodeRepository;
    private final EmbeddingGateway embeddingGateway;
    private final StructureExtractionProcessor structureExtractionProcessor;

    public EmbeddingProcessor(StructureExtractionMissionRepository structureExtractionMissionRepository, EmbeddingMissionRepository embeddingMissionRepository, NodeRepository nodeRepository, EmbeddingGateway embeddingGateway, StructureExtractionProcessor structureExtractionProcessor) {
        this.structureExtractionMissionRepository = structureExtractionMissionRepository;
        this.embeddingMissionRepository = embeddingMissionRepository;
        this.nodeRepository = nodeRepository;
        this.embeddingGateway = embeddingGateway;
        this.structureExtractionProcessor = structureExtractionProcessor;
    }

    public void process(EmbeddingMission embeddingMission) {
        embeddingMissionRepository.save(embeddingMission);

        // 1. 获取并检查结构提取任务状态
        List<StructureExtractionMission> structureExtractionMissions = structureExtractionMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());
        if (hasRunningOrPendingStructureExtraction(structureExtractionMissions)) {
            embeddingMission.failure("存在运行中或等待中的结构提取任务，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return;
        }

        // 2. 获取或执行结构提取任务
        StructureExtractionMission structureExtractionMission = getOrCreateStructureExtractionMission(embeddingMission.getSourceDocumentId(), structureExtractionMissions);
        if (!structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return;
        }

        // 3. 执行向量化
        executeEmbedding(embeddingMission, structureExtractionMission);
    }

    private boolean hasRunningOrPendingStructureExtraction(List<StructureExtractionMission> missions) {
        return missions.stream()
                .anyMatch(mission -> MissionStatus.RUNNING.equals(mission.getStatus()) ||
                        MissionStatus.PENDING.equals(mission.getStatus()));
    }

    private StructureExtractionMission getOrCreateStructureExtractionMission(String sourceDocumentId, List<StructureExtractionMission> missions) {
        return missions.stream()
                .filter(StructureExtractionMission::isSuccess)
                .findFirst()
                .orElseGet(() -> {
                    StructureExtractionMission newMission = StructureExtractionMission.Companion.create(UUID.randomUUID().toString(), sourceDocumentId);
                    structureExtractionProcessor.process(newMission);
                    return newMission;
                });
    }

    private void executeEmbedding(EmbeddingMission embeddingMission, StructureExtractionMission structureExtractionMission) {
        StructureExtractionMissionResult.Success successResult = structureExtractionMission.getSuccessResult();
        embeddingMission.start(successResult.getFileNodeId());

        try {
            doEmbed(embeddingMission, successResult.getFileNodeId());
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            embeddingMission.failure(msg);
        }

        embeddingMissionRepository.save(embeddingMission);
    }

    private void doEmbed(EmbeddingMission embeddingMission, String fileNodeId) throws Exception {
        // 成功提取结构后的FileNode唯一Id
        fileNodeId = Objects.requireNonNull(fileNodeId);
        Optional<FileNode> fileNodeOptional = nodeRepository.findFileNodeByFileNodeId(fileNodeId);
        if (fileNodeOptional.isEmpty())
            throw new IllegalAccessException("结构提取任务存在，但是切割后的FileNode不存在");

        FileNode fileNode = fileNodeOptional.get();
        // 找到所有的TextNode
        List<TextNode> textNodeList = nodeRepository.findTextNodeListByFileNodeId(fileNodeId);

        // 向量化所有text属性
        List<List<Float>> vectors = embeddingGateway.embed(textNodeList.stream().map(TextNode::getText).toList());

        // 为所有的TextNode添加vector属性
        for (int i = 0; i < textNodeList.size(); i++) {
            textNodeList.get(i).saveVector(vectors.get(i));
        }

        // 为FileNode标记状态
        fileNode.setEmbedded(true);

        // 保存
        textNodeList.forEach(nodeRepository::saveTextNode);
        nodeRepository.saveFileNode(fileNode);
        embeddingMission.success(fileNodeId);
    }
}
