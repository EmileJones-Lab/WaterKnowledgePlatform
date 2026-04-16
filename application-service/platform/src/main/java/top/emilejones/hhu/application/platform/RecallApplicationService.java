package top.emilejones.hhu.application.platform;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.repository.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.result.TextNode;
import top.emilejones.hhu.domain.pipeline.repository.NodeRepository;
import top.emilejones.hhu.domain.pipeline.repository.TextNodeVectorRepository;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class RecallApplicationService {
    private final NodeRepository nodeRepository;
    private final TextNodeVectorRepository textNodeVectorRepository;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;

    public RecallApplicationService(NodeRepository nodeRepository,
                                    TextNodeVectorRepository textNodeVectorRepository,
                                    KnowledgeCatalogRepository knowledgeCatalogRepository) {
        this.nodeRepository = nodeRepository;
        this.textNodeVectorRepository = textNodeVectorRepository;
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
    }

    /**
     * 根据问题召回相关文本
     *
     * @param query              问题
     * @param knowledgeCatalogId 知识库
     * @return 知识库中和问题相关的文本
     */
    public List<String> recallText(String query, String knowledgeCatalogId) {
        KnowledgeCatalog knowledgeCatalog = knowledgeCatalogRepository.find(knowledgeCatalogId);
        String milvusCollectionName = Objects.requireNonNull(knowledgeCatalog).getMilvusCollectionName();
        List<TextNode> textNodes = textNodeVectorRepository.recallTextNode(query, milvusCollectionName);
        return textNodes.stream().map(TextNode::getText).toList();
    }
}
