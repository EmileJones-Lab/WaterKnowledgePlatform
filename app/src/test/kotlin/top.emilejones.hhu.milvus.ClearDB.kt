import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.collection.request.DropCollectionReq
import top.emilejones.huu.env.MilvusEnvironment

fun main() {
    val client = MilvusClientV2(
        ConnectConfig.builder()
            .uri("http://${MilvusEnvironment.HOST}:${MilvusEnvironment.PORT}")
            .build()
    )
    val dropQuickSetupParam = DropCollectionReq.builder()
        .collectionName(MilvusEnvironment.COLLECTION_NAME)
        .databaseName(MilvusEnvironment.DATABASE_NAME)
        .build()

    client.dropCollection(dropQuickSetupParam)
}