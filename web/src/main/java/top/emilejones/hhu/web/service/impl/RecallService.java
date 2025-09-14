package top.emilejones.hhu.web.service.impl;

import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.pojo.RerankResult;
import top.emilejones.hhu.spliter.java.HtmlTableToCsvSplitterForJava;
import top.emilejones.hhu.web.entity.MilvusDatum;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.enums.TextType;
import top.emilejones.hhu.web.repository.IMilvusRepository;
import top.emilejones.hhu.web.repository.INeo4jRepository;
import top.emilejones.hhu.web.service.IRecallService;
import top.emilejones.huu.env.pojo.ApplicationConfig;

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
    private static Logger logger = LoggerFactory.getLogger(RecallService.class);
    private final ApplicationConfig config;

    public RecallService(IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ApplicationConfig config, ModelClient client) {
        this.milvusRepository = milvusRepository;
        this.neo4jRepository = neo4jRepository;
        this.config = config;
        this.client = client;
    }

    @Override
    public List<String> recallText(String query) {
        final int maxResultNumber = config.getRag().getRecallNumber();

        // 从向量数据库中召回数据
        List<Float> queryVector = client.embedding(query);
        List<MilvusDatum> searchResults = milvusRepository.search(queryVector, 100);
        // 将召回的结果分批rerank（由于显存问题，需要分批rerank）
        final int step = 5;
        int index = 0;
        List<Pair<RerankResult, MilvusDatum>> rerankResults = new ArrayList<>();

        while (index < searchResults.size()) {
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < step; i++) {
                strings.add(searchResults.get(index + i).getText());
            }
            int startIndex = index;
            List<Pair<RerankResult, MilvusDatum>> pairList = client.rerank(query, strings).stream().map(result -> {
                int index1 = result.getIndex();
                return new Pair<>(result, searchResults.get(index1 + startIndex));
            }).toList();
            rerankResults.addAll(pairList);
            index += step;
        }
        // 将分批rerank后的结果排序，获取得分最高的maxResultNumber个结果
        Set<MilvusDatum> sets = rerankResults.stream()
                .sorted(Comparator.comparingDouble(value -> {
                    Pair<RerankResult, TextNode> value1 = (Pair<RerankResult, TextNode>) value;
                    return value1.getFirst().getScore();
                }).reversed())
                .limit(maxResultNumber)
                .map(obj -> {
                    Pair<RerankResult, MilvusDatum> pair = (Pair<RerankResult, MilvusDatum>) obj;
                    return pair.getSecond();
                })
                .collect(Collectors.toSet());
        // 将每一个节点向上向下查找，如果有表格上下文则加入
        Set<TextNode> resultSet = new HashSet<>();
        for (MilvusDatum milvusDatum : sets) {
            TextNode node = neo4jRepository.selectByElementId(milvusDatum.getElementId());
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
        long tableCount = resultSet.stream().filter(result -> TextType.TABLE.equals(result.getType())).count();
        // 将所有html表格压缩为csv格式
        resultSet = resultSet.stream()
                .map(result -> {
                    if (!TextType.TABLE.equals(result.getType())) {
                        return result;
                    }
                    List<String> split = HtmlTableToCsvSplitterForJava.INSTANCE.split(result.getText(), Integer.MAX_VALUE);
                    result.setText(split.get(0));
                    return result;
                }).collect(Collectors.toSet());

        logger.info("The recall text list is [{}]", resultSet.stream().map(result -> "\"" + result.getText() + "\"").collect(Collectors.joining(", ")));
        return resultSet.stream().map(TextNode::getText).toList();
    }
}
