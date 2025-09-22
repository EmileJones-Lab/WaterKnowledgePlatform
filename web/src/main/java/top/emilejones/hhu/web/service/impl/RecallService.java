package top.emilejones.hhu.web.service.impl;

import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.model.pojo.RerankResult;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.MilvusDatum;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.repository.IMilvusRepository;
import top.emilejones.hhu.web.repository.INeo4jRepository;
import top.emilejones.hhu.web.service.IRecallService;
import top.emilejones.hhu.web.service.strategy.HtmlTableToCsvStrategy;
import top.emilejones.hhu.web.service.strategy.ObtainWholeTableStrategy;
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author EmileJones
 */
@Service
public class RecallService implements IRecallService {
    private IMilvusRepository milvusRepository;
    private INeo4jRepository neo4jRepository;
    private ModelClient client;
    private static Logger logger = LoggerFactory.getLogger(RecallService.class);
    private final ApplicationConfig config;

    private final ObtainWholeTableStrategy obtainWholeTableStrategy = new ObtainWholeTableStrategy(neo4jRepository);
    private final HtmlTableToCsvStrategy htmlTableToCsvStrategy = new HtmlTableToCsvStrategy();

    public RecallService(IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ApplicationConfig config, ModelClient client) {
        this.milvusRepository = milvusRepository;
        this.neo4jRepository = neo4jRepository;
        this.config = config;
        this.client = client;
    }

    @Override
    public List<String> recallText(String query) {
        return recallNode(query).stream().map(datum -> datum.getSecond().getText()).toList();
    }

    @Override
    public List<Pair<FileNode, TextNode>> recallNode(String query) {
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
            for (int i = 0; i < step && index + i < searchResults.size(); i++) {
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
        // 将milvus数据转换为neo4j数据
        List<Pair<FileNode, TextNode>> rawData = sets.stream().map(milvusDatum -> neo4jRepository.selectByElementId(milvusDatum.getElementId())).toList();
        // 将每一个表格节点向上向下查找，如果有表格上下文则加入
        List<Pair<FileNode, TextNode>> wholeTableData = obtainWholeTableStrategy.exec(rawData);
        // 将所有html表格压缩为csv格式
        List<Pair<FileNode, TextNode>> csvTableData = htmlTableToCsvStrategy.exec(wholeTableData);
        logger.info("召回的节点数据为: {}", csvTableData);
        return csvTableData;
    }
}
