package top.emilejones.hhu.application.processor;

import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.FileNode;
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

    public EmbeddingMission process(EmbeddingMission embeddingMission) {
        embeddingMissionRepository.save(embeddingMission);
        // 获取曾经执行过的结构提取任务，如果没有执行成功的结构提取任务，则开启一个新的结构提取任务。
        List<StructureExtractionMission> structureExtractionMissions = structureExtractionMissionRepository.findBySourceDocumentId(embeddingMission.getSourceDocumentId());
        Optional<StructureExtractionMission> succeesfulStructureExtractionOptional = structureExtractionMissions.stream().filter(StructureExtractionMission::isSuccess).findFirst();
        StructureExtractionMission structureExtractionMission = succeesfulStructureExtractionOptional.orElseGet(() -> {
                    StructureExtractionMission newMission = StructureExtractionMission.Companion.create(UUID.randomUUID().toString(), embeddingMission.getSourceDocumentId());
                    return structureExtractionProcessor.process(newMission);
                }
        );
        if (!structureExtractionMission.isSuccess()) {
            embeddingMission.failure("结构提取任务未成功，无法开启向量化任务");
            embeddingMissionRepository.save(embeddingMission);
            return embeddingMission;
        }

        // 根据之前的结构提取结果进行向量化任务
        StructureExtractionMissionResult.Success successResult = structureExtractionMission.getSuccessResult();
        embeddingMission.start(successResult.getFileNodeId());
        try {
            // 成功提取结构后的FileNode唯一Id
            String fileNodeId = Objects.requireNonNull(embeddingMission.getFileNodeId());
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
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "未知的异常";
            embeddingMission.failure(msg);
        }
        embeddingMissionRepository.save(embeddingMission);
        return embeddingMission;
    }
}
