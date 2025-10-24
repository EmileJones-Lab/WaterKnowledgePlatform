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
import top.emilejones.huu.env.pojo.ApplicationConfig;

import java.util.List;

/**
 * @author EmileJones
 */
@Service
public class RecallService implements IRecallService {
    private final IMilvusRepository milvusRepository;
    private final INeo4jRepository neo4jRepository;
    private final ModelClient client;
    private static final Logger logger = LoggerFactory.getLogger(RecallService.class);
    private final ApplicationConfig config;

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
        // 重排序结果，并取出得分最高的maxResultNumber个数据
        List<MilvusDatum> rerankResult = client.rerank(query, searchResults.stream().map(MilvusDatum::getText).toList())
                .stream()
                .limit(maxResultNumber)
                .map(rr -> searchResults.get(rr.getIndex())).toList();
        // 将milvus数据转换为neo4j数据
        List<Pair<FileNode, TextNode>> rawData = rerankResult.stream().map(milvusDatum -> neo4jRepository.selectByElementId(milvusDatum.getElementId())).toList();
        logger.info("用户问题为：[{}]，召回的节点数量为[{}]个", query, rawData.size());
        return rawData;
    }
}
