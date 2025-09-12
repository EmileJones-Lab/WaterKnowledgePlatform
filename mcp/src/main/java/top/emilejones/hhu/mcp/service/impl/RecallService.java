package top.emilejones.hhu.mcp.service.impl;

import kotlin.Pair;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.mcp.entity.TextNode;
import top.emilejones.hhu.mcp.enums.TextType;
import top.emilejones.hhu.mcp.repository.IMilvusRepository;
import top.emilejones.hhu.mcp.repository.INeo4jRepository;
import top.emilejones.hhu.mcp.service.IRecallService;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.impl.XinferenceHttpClient;
import top.emilejones.hhu.model.pojo.RerankResult;
import top.emilejones.huu.env.XinferenceEnvironment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用来负责处理召回任务
 *
 * @author EmileJones
 */
@Service
public class RecallService implements IRecallService {
    private IMilvusRepository milvusRepository;
    private INeo4jRepository neo4jRepository;
    private ModelClient client;

    public RecallService(IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository) {
        this.milvusRepository = milvusRepository;
        this.neo4jRepository = neo4jRepository;
        client = new XinferenceHttpClient(XinferenceEnvironment.HOST, XinferenceEnvironment.PORT);
    }

    @Override
    @Tool(description = "根据问题返回和问题最相关的资料片段")
    public List<String> recallText(@ToolParam(description = "问题") String query) {
        final int maxResultNumber = 5;

        // 从向量数据库中召回数据
        List<Float> queryVector = client.embedding(query);
        List<TextNode> searchResults = milvusRepository.search(queryVector, 100);
        // 将召回的结果分批rerank（由于显存问题，需要分批rerank）
        final int step = 5;
        int index = 0;
        List<Pair<RerankResult, TextNode>> rerankResults = new ArrayList<>();

        while (index < searchResults.size()) {
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < step; i++) {
                strings.add(searchResults.get(index + i).getText());
            }
            int startIndex = index;
            List<Pair<RerankResult, TextNode>> pairList = client.rerank(query, strings).stream().map(result -> {
                int index1 = result.getIndex();
                return new Pair<>(result, searchResults.get(index1 + startIndex));
            }).toList();
            rerankResults.addAll(pairList);
            index += step;
        }
        // 将分批rerank后的结果排序，获取得分最高的maxResultNumber个结果
        Set<TextNode> sets = rerankResults.stream()
                .sorted(Comparator.comparingDouble(value -> {
                    Pair<RerankResult, TextNode> value1 = (Pair<RerankResult, TextNode>) value;
                    return value1.getFirst().getScore();
                }).reversed())
                .limit(maxResultNumber)
                .map(obj -> {
                    Pair<RerankResult, TextNode> pair = (Pair<RerankResult, TextNode>) obj;
                    return pair.getSecond();
                })
                .collect(Collectors.toSet());
        // 将每一个节点向上向下查找，如果有表格上下文则加入
        Set<TextNode> resultSet = new HashSet<>();
        for (TextNode node : sets) {
            resultSet.add(node);
            // 向下找
            TextNode nowNode = node;
            while (nowNode.getType() == TextType.TABLE) {
                resultSet.add(nowNode);
                nowNode = neo4jRepository.nextNode(nowNode.getElementId());
                if (resultSet.contains(nowNode))
                    break;
            }
            // 向上找
            nowNode = node;
            while (nowNode.getType() == TextType.TABLE) {
                resultSet.add(nowNode);
                nowNode = neo4jRepository.preNode(nowNode.getElementId());
                if (resultSet.contains(nowNode))
                    break;
            }
        }

        return resultSet.stream().map(TextNode::getText).toList();
    }
}
