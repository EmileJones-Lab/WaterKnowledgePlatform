package top.emilejones.hhu.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.emilejones.hhu.domain.knowledge.KnowledgeCatalog;
import top.emilejones.hhu.domain.knowledge.infrastructure.KnowledgeCatalogRepository;
import top.emilejones.hhu.domain.pipeline.TextNode;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.NodeRepository;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class RecallApplicationService {
    private final NodeRepository nodeRepository;
    private final KnowledgeCatalogRepository knowledgeCatalogRepository;

    public RecallApplicationService(NodeRepository nodeRepository, KnowledgeCatalogRepository knowledgeCatalogRepository) {
        this.nodeRepository = nodeRepository;
        this.knowledgeCatalogRepository = knowledgeCatalogRepository;
    }

    public List<String> recallText(String query, String knowledgeCatalogId) {
        KnowledgeCatalog knowledgeCatalog = knowledgeCatalogRepository.find(knowledgeCatalogId);
        String milvusCollectionName = knowledgeCatalog.getMilvusCollectionName();
        List<TextNode> textNodes = nodeRepository.recallTextNode(query, milvusCollectionName);
        return textNodes.stream().map(TextNode::getText).toList();
    }
}
