package top.emilejones.hhu.web.service.impl;

import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.model.ModelClient;
import top.emilejones.hhu.web.entity.FileNode;
import top.emilejones.hhu.web.entity.MilvusDatum;
import top.emilejones.hhu.web.entity.TextNode;
import top.emilejones.hhu.web.repository.IMilvusRepository;
import top.emilejones.hhu.web.repository.INeo4jRepository;
import top.emilejones.hhu.web.service.IRecallService;
import top.emilejones.hhu.web.service.strategy.HtmlTableToCsvStrategy;
import top.emilejones.hhu.web.service.strategy.ObtainWholeTableStrategy;
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.List;

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

    private final ObtainWholeTableStrategy obtainWholeTableStrategy;
    private final HtmlTableToCsvStrategy htmlTableToCsvStrategy;

    public RecallService(IMilvusRepository milvusRepository, INeo4jRepository neo4jRepository, ApplicationConfig config, ModelClient client) {
        this.milvusRepository = milvusRepository;
        this.neo4jRepository = neo4jRepository;
        this.config = config;
        this.client = client;
        obtainWholeTableStrategy = new ObtainWholeTableStrategy(neo4jRepository);
        htmlTableToCsvStrategy = new HtmlTableToCsvStrategy();
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
        // 重排序结果，并取出得分最高的maxResultNumber个数据
        List<MilvusDatum> rerankResult = client.rerank(query, searchResults.stream().map(MilvusDatum::getText).toList())
                .stream()
                .limit(maxResultNumber)
                .map(rr -> {
                    return searchResults.get(rr.getIndex());
                }).toList();
        // 将milvus数据转换为neo4j数据
        List<Pair<FileNode, TextNode>> rawData = rerankResult.stream().map(milvusDatum -> neo4jRepository.selectByElementId(milvusDatum.getElementId())).toList();
        // 将每一个表格节点向上向下查找，如果有表格上下文则加入
        List<Pair<FileNode, TextNode>> wholeTableData = obtainWholeTableStrategy.exec(rawData);
        // 将所有html表格压缩为csv格式
        List<Pair<FileNode, TextNode>> csvTableData = htmlTableToCsvStrategy.exec(wholeTableData);
        logger.info("用户问题为：[{}]，召回的节点数量为[{}]个", query, csvTableData.size());
        return csvTableData;
    }
}
