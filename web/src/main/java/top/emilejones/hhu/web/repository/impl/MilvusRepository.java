package top.emilejones.hhu.web.repository.impl;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.stereotype.Repository;
import top.emilejones.hhu.web.entity.MilvusDatum;
import top.emilejones.hhu.web.enums.TextType;
import top.emilejones.hhu.web.repository.IMilvusRepository;
import top.emilejones.huu.env.MilvusEnvironment;

import java.util.List;

/**
 * 向量数据库的实现
 *
 * @author EmileJones
 */
@Repository
public class MilvusRepository implements IMilvusRepository {
    private final MilvusClientV2 client;
    private final String databaseName;
    private final String collectionName;

    public MilvusRepository() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://%s:%d".formatted(MilvusEnvironment.HOST, MilvusEnvironment.PORT))
                .build();
        client = new MilvusClientV2(connectConfig);
        databaseName = MilvusEnvironment.DATABASE_NAME;
        collectionName = MilvusEnvironment.COLLECTION_NAME;
    }

    @Override
    public List<MilvusDatum> search(List<Float> queryVector, int topK) {
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
            MilvusDatum datum = new MilvusDatum();
            datum.setElementId(result.getEntity().get("elementId").toString());
            datum.setText(result.getEntity().get("text").toString());
            datum.setType(TextType.valueOf(result.getEntity().get("type").toString()));
            return datum;
        })).toList();
    }
}
