package top.emilejones.hhu.tools

import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.service.collection.request.DropCollectionReq
import top.emilejones.huu.env.AutoFindConfigFile

fun main() {
    val config = AutoFindConfigFile.find()
    val client = MilvusClientV2(
        ConnectConfig.builder()
            .uri("http://${config.milvus.host}:${config.milvus.port}")
            .build()
    )
    val dropQuickSetupParam = DropCollectionReq.builder()
        .collectionName(config.milvus.collection)
        .databaseName(config.milvus.database)
        .build()
    client.dropCollection(dropQuickSetupParam)
}