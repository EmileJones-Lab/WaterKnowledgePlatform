package top.emilejones.hhu.application.command;

import top.emilejones.hhu.application.command.dto.TextNodeDTO;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.application.command.record.ProcessRecordService;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository;
import top.emilejones.hhu.domain.result.TextNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 召回应用服务。
 * 负责从向量数据库中召回与查询最相关的文本内容。
 */
@Service
public class RecallApplicationService {

    private final NodeRepository nodeRepository;
    private final TextNodeVectorRepository textNodeVectorRepository;
    private final ProcessRecordService processRecordService;

    public RecallApplicationService(NodeRepository nodeRepository,
                                    TextNodeVectorRepository textNodeVectorRepository,
                                    ProcessRecordService processRecordService) {
        this.nodeRepository = nodeRepository;
        this.textNodeVectorRepository = textNodeVectorRepository;
        this.processRecordService = processRecordService;
    }

    /**
     * 根据查询语句召回相关的文本内容。
     *
     * @param query 查询字符串
     * @return 与查询最相关的文本列表
     */
    public List<String> recallText(String query) {
        List<TextNode> textNodes = textNodeVectorRepository.recallTextNode(query, EmbeddingApplicationService.COLLECTION_NAME, null);
        return textNodes.stream()
                .map(TextNode::getText)
                .collect(Collectors.toList());
    }

    /**
     * 根据查询语句召回相关的文本节点。
     *
     * @param query 查询字符串
     * @return 与查询最相关的文本节点列表
     */
    public List<TextNodeDTO> recallTextNodes(String query) {
        List<TextNode> textNodes = textNodeVectorRepository.recallTextNode(query, EmbeddingApplicationService.COLLECTION_NAME, null);
        return textNodes.stream()
                .map(node -> TextNodeDTO.builder()
                        .id(node.getId())
                        .fileNodeId(node.getFileNodeId())
                        .text(node.getText())
                        .seq(node.getSeq())
                        .level(node.getLevel())
                        .type(node.getType().name())
                        .fileName(processRecordService.getFileNameByFileNodeId(node.getFileNodeId()))
                        .summary(node.getSummary())
                        .build())
                .collect(Collectors.toList());
    }
}
