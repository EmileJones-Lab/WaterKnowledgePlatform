package top.emilejones.hhu.service.impl;

import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.emilejones.hhu.domain.dto.DenseRecallResult;
import top.emilejones.hhu.entity.FileNode;
import top.emilejones.hhu.entity.TextNode;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.repository.IMilvusRepository;
import top.emilejones.hhu.repository.INeo4jRepository;
import top.emilejones.hhu.service.IRecallService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author EmileJones
 */
public class RecallService implements IRecallService {
    private final IMilvusRepository milvusRepository;
    private final INeo4jRepository neo4jRepository;
    private final ModelClient client;
    private final Integer recallNumber;
    private static final Logger logger = LoggerFactory.getLogger(RecallService.class);

    public RecallService(IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ModelClient client, Integer recallNumber) {
        this.milvusRepository = milvusRepository;
        this.neo4jRepository = neo4jRepository;
        this.client = client;
        this.recallNumber = recallNumber;
    }

    @Override
    public List<String> recallText(String query) {
        return recallNode(query).stream().map(datum -> datum.getSecond().getText()).toList();
    }

    @Override
    public List<Pair<FileNode, TextNode>> recallNode(String query) {
        final int maxResultNumber = recallNumber;

        // 从向量数据库中召回数据
        List<Float> queryVector = client.embedding(query);
        List<DenseRecallResult> searchResults = milvusRepository.search(queryVector, 100);
        // 重排序结果，并取出得分最高的maxResultNumber个数据
        List<DenseRecallResult> rerankResult = client.rerank(query, searchResults.stream().map(DenseRecallResult::getText).toList())
                .stream()
                .limit(maxResultNumber)
                .map(rr -> searchResults.get(rr.getIndex())).toList();
        // 将milvus数据转换为neo4j数据
        List<Pair<FileNode, TextNode>> rawData = rerankResult.stream().map(denseRecallResult -> neo4jRepository.selectByElementId(denseRecallResult.getElementId())).toList();
        logger.debug("查询问题[{}]召回节点的cypher语句为[{}]", query, generateCypherByTextNodeList(rawData.stream().map(Pair::getSecond).toList()));
        logger.info("用户问题为：[{}]，召回的节点数量为[{}]个", query, rawData.size());
        return rawData;
    }

    private String generateCypherByTextNodeList(List<TextNode> textNodeList) {
        String list = textNodeList.stream()
                .map(TextNode::getElementId)
                .map(it -> "\"" + it + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
        return "MATCH (n: TextNode) WHERE elementId(n) IN %s MATCH (f:FileNode)-[:CONTAIN]->(n) RETURN n,f".formatted(list);
    }
}
