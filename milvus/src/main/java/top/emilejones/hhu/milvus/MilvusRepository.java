package top.emilejones.hhu.milvus;


import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import top.emilejones.hhu.entity.DenseRecallResult;
import top.emilejones.hhu.enums.TextType;
import top.emilejones.hhu.repository.IMilvusRepository;

import java.util.List;

/**
 * 向量数据库的实现
 *
 * @author EmileJones
 */
public class MilvusRepository implements IMilvusRepository {
    private final MilvusClientV2 client;
    private final String databaseName;
    private final String collectionName;

    public MilvusRepository(String host, Integer port, String database, String collection) {

        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://%s:%d".formatted(host, port))
                .build();
        this.client = new MilvusClientV2(connectConfig);
        this.databaseName = database;
        this.collectionName = collection;
    }

    @Override
    public List<DenseRecallResult> search(List<Float> queryVector, int topK) {
        FloatVec floatVec = new FloatVec(queryVector);
        SearchResp searchR = client.search(SearchReq.builder()
                .databaseName(databaseName)
                .collectionName(collectionName)
                .data(List.of(floatVec))
                .topK(topK)
                .outputFields(List.of("text", "elementId", "type"))
                .build());
        List<List<SearchResp.SearchResult>> searchResults = searchR.getSearchResults();
        return searchResults.stream().flatMap(list -> list.stream().map(result -> {
            DenseRecallResult datum = new DenseRecallResult();
            datum.setElementId(result.getEntity().get("elementId").toString());
            datum.setText(result.getEntity().get("text").toString());
            datum.setType(TextType.valueOf(result.getEntity().get("type").toString()));
            datum.setScore(result.getScore());
            return datum;
        })).toList();
    }
}
