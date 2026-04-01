package top.emilejones.hhu.application.command;

import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
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

    public RecallApplicationService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    /**
     * 根据查询语句召回相关的文本内容。
     *
     * @param query 查询字符串
     * @return 与查询最相关的文本列表
     */
    public List<String> recallText(String query) {
        List<TextNode> textNodes = nodeRepository.recallTextNode(query, EmbeddingApplicationService.COLLECTION_NAME);
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
    public List<TextNode> recallTextNodes(String query) {
        return nodeRepository.recallTextNode(query, EmbeddingApplicationService.COLLECTION_NAME);
    }
}
