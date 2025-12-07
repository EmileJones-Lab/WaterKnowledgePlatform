package top.emilejones.hhu.textsplitter.repository.impl.milvus

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.DataType
import io.milvus.v2.common.IndexParam
import io.milvus.v2.service.collection.request.AddFieldReq
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.DropCollectionReq
import io.milvus.v2.service.database.request.CreateDatabaseReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.QueryReq
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.UpsertReq
import io.milvus.v2.service.vector.request.data.BaseVector
import io.milvus.v2.service.vector.request.data.FloatVec
import io.milvus.v2.service.vector.response.SearchResp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.emilejones.hhu.domain.pipeline.TextType
import top.emilejones.hhu.textsplitter.domain.po.EmbeddingDatum
import top.emilejones.hhu.env.pojo.HttpModelClientConfig
import top.emilejones.hhu.env.pojo.MilvusConfig
import top.emilejones.hhu.textsplitter.repository.IMultiCollectionMilvusRepository


@Service
class MultiCollectionSingleCollectionMilvusRepository(
    private val client: MilvusClientV2,
    milvusConfig: MilvusConfig,
    httpModelClientConfig: HttpModelClientConfig
) : IMultiCollectionMilvusRepository {
    private val databaseName: String = milvusConfig.database
    private val dimension: Int = httpModelClientConfig.dimension
    private val existsCollection: MutableSet<String> = HashSet()
    private val logger = LoggerFactory.getLogger(MultiCollectionSingleCollectionMilvusRepository::class.java)
    private val gson = Gson()

    init {
        createDatabaseIfNotExists()
        client.useDatabase(databaseName)
    }

    override fun insert(collectionName: String, datum: EmbeddingDatum): Boolean {
        checkCollectionExistOrCreate(collectionName)
        logger.trace("Start insert embedding node [{}] ", datum.neo4jNodeId)
        // 1. 封装向量为 FloatArray
        val vectorArray: FloatArray = datum.vector.toFloatArray()

        // 2. 构建单条数据的 JsonObject
        val jsonData = datum.toJsonObject(vectorArray)

        // 3. 构建 InsertReq
        val insertReq = InsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(listOf(jsonData))
            .build()

        // 4. 执行插入
        val resp = client.insert(insertReq)
        logger.trace("Success insert embedding node, primary key is [{}] ", resp.primaryKeys[0])
        return true
    }

    override fun batchInsert(collectionName: String, data: List<EmbeddingDatum>): Boolean {
        checkCollectionExistOrCreate(collectionName)
        logger.trace("Start batch insert embedding nodes, nodes number: [{}]", data.size)
        val jsonObjectList = data.map { datum ->
            // 封装向量为 FloatArray
            val vectorArray: FloatArray = datum.vector.toFloatArray()

            // 构建单条数据的 JsonObject
            datum.toJsonObject(vectorArray)
        }.toMutableList()

        // 3. 构建 InsertReq
        val insertReq = InsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(jsonObjectList)
            .build()

        // 4. 执行插入
        val resp = client.insert(insertReq)
        logger.trace("Success batch insert embedding nodes, nodes number: [{}] ", resp.insertCnt)
        return true
    }

    override fun batchDelete(collectionName: String, ids: List<String>): Boolean {
        if (ids.isEmpty()) {
            logger.debug("Batch delete called with empty ids for collection [{}]", collectionName)
            return true
        }
        if (!ensureCollectionExists(collectionName)) {
            logger.warn("Collection [{}] does not exist, skip delete for ids [{}]", collectionName, ids)
            return false
        }
        val queryReq = QueryReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .ids(ids)
            .outputFields(listOf("neo4jNodeId", "text", "vector", "type", "isDelete"))
            .build()
        val queryResults = client.query(queryReq).queryResults ?: emptyList()
        if (queryResults.isEmpty()) {
            logger.warn("No embedding nodes found in collection [{}] for ids [{}]", collectionName, ids)
            return true
        }

        val tombstones = queryResults.mapNotNull { result ->
            mapToEmbeddingDatum(result.entity, overrideIsDelete = true)
        }
        if (tombstones.isEmpty()) {
            logger.warn("Unable to map any embedding nodes for soft delete in collection [{}]", collectionName)
            return false
        }

        val upsertReq = UpsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(tombstones.map { it.toJsonObject(it.vector.toFloatArray()) })
            .build()
        client.upsert(upsertReq)
        logger.trace("Soft deleted [{}] embedding nodes in collection [{}]", tombstones.size, collectionName)
        return true
    }

    override fun searchByVector(
        collectionName: String,
        queryVector: List<Float>,
        topK: Int,
        filter: String?
    ): List<EmbeddingDatum> {
        val mutableListOf: MutableList<BaseVector> = mutableListOf(FloatVec(queryVector))
        val searchParamsMap: Map<String, Any> = mapOf(
            "metric_type" to "L2", "params" to mapOf("nprobe" to 10)
        )
        // 构建搜索请求
        val searchReq =
            SearchReq.builder().databaseName(databaseName).collectionName(collectionName).annsField("vector").topK(topK)
                .data(mutableListOf).apply {
                    filter(buildFilter(filter))
                }.outputFields(listOf("text", "neo4jNodeId", "vector", "type", "isDelete"))
                .searchParams(searchParamsMap).build()

        // 发起搜索
        val resp = client.search(searchReq)
        val data = resp.searchResults ?: return emptyList()


        val resultsForQuery: List<SearchResp.SearchResult> = data.firstOrNull() ?: return emptyList()
        // 6. 映射成 EmbeddingDatum
        return resultsForQuery.mapNotNull { r -> mapToEmbeddingDatum(r.entity) }
    }

    override fun clearAllData(collectionName: String) {
        val dropQuickSetupParam = DropCollectionReq.builder()
            .collectionName(collectionName)
            .databaseName(databaseName)
            .build()
        client.dropCollection(dropQuickSetupParam)
    }

    private fun checkCollectionExistOrCreate(collectionName: String) {
        if (existsCollection.contains(collectionName))
            return
        val resp = client.listCollections()
        if (!resp.collectionNames.contains(collectionName))
            createCollection(collectionName)
        existsCollection.add(collectionName)
    }

    private fun createDatabaseIfNotExists() {
        val resp = client.listDatabases() // 列出所有数据库
        if (resp.databaseNames.contains(databaseName))
            return
        val createDatabaseReq = CreateDatabaseReq.builder().databaseName(databaseName).build()
        client.createDatabase(createDatabaseReq)
        logger.debug("Create milvus database named [{}]", databaseName)
    }

    private fun createCollection(collectionName: String) {
        // 1. 创建schema
        val schema = MilvusClientV2.CreateSchema()
        // 2. 创建字段
        // 2.1 neo4jNodeId (VarChar 主键)
        schema.addField(
            AddFieldReq.builder().fieldName("neo4jNodeId").dataType(DataType.VarChar).isPrimaryKey(true).autoID(false)
                .maxLength(36) // 必须指定长度
                .build()
        )

        // 2.2 text (VarChar 存储原始文本)
        schema.addField(
            AddFieldReq.builder().fieldName("text").dataType(DataType.VarChar).maxLength(4096).build()
        )

        // 2.3 vector (FloatVector，存储向量)
        schema.addField(
            AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(dimension).build()
        )

        // 2.4 type (VarChar 存储原始文本)
        schema.addField(
            AddFieldReq.builder().fieldName("type").dataType(DataType.VarChar).maxLength(20).build()
        )
        // 2.5 isDelete (Bool 软删除标记)
        schema.addField(
            AddFieldReq.builder().fieldName("isDelete").dataType(DataType.Bool).defaultValue(false)
                .enableDefaultValue(true).build()
        )
        // 3. 索引
        val indexParams = listOf(
            IndexParam.builder().fieldName("vector").indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE).build()
        )

        // 4. 创建 Collection
        val createReq = CreateCollectionReq.builder().collectionSchema(schema).collectionName(collectionName)
            .indexParams(indexParams).databaseName(databaseName).build()

        client.createCollection(createReq)
        logger.debug("Create milvus collection named [{}]", collectionName)
    }

    private fun buildFilter(filter: String?): String {
        val base = "isDelete == false"
        return if (filter.isNullOrBlank()) base else "$base && ($filter)"
    }

    private fun mapToEmbeddingDatum(entity: Map<String, Any?>, overrideIsDelete: Boolean? = null): EmbeddingDatum? {
        val vectorValue = entity["vector"] ?: return null
        val vector = convertVector(vectorValue) ?: return null
        val neo4jId = entity["neo4jNodeId"]?.toString() ?: return null
        val text = entity["text"]?.toString() ?: return null
        val typeString = entity["type"]?.toString() ?: return null
        val isDelete = overrideIsDelete ?: (entity["isDelete"] as? Boolean ?: false)
        return kotlin.runCatching { TextType.valueOf(typeString) }.map {
            EmbeddingDatum(vector = vector, neo4jNodeId = neo4jId, text = text, type = it, isDelete = isDelete)
        }.getOrElse {
            logger.warn("Unknown text type [{}] for node [{}]", typeString, neo4jId)
            null
        }
    }

    private fun convertVector(vector: Any): List<Float>? {
        val source: List<*> = when (vector) {
            is List<*> -> vector
            is FloatArray -> vector.toList()
            is DoubleArray -> vector.map { it }
            else -> {
                logger.warn("Illegal vector type [{}]", vector::class)
                return null
            }
        }
        val converted = source.mapNotNull {
            if (it !is Number) {
                logger.warn("Illegal vector element type [{}]", it?.let { v -> v::class })
                return null
            }
            it.toFloat()
        }
        return converted
    }

    private fun EmbeddingDatum.toJsonObject(vectorArray: FloatArray): JsonObject {
        return JsonObject().apply {
            add("vector", gson.toJsonTree(vectorArray))
            addProperty("neo4jNodeId", neo4jNodeId)
            addProperty("text", text)
            addProperty("type", type.name)
            addProperty("isDelete", isDelete)
        }
    }

    private fun ensureCollectionExists(collectionName: String): Boolean {
        if (existsCollection.contains(collectionName)) {
            return true
        }
        val resp = client.listCollections()
        val exists = resp.collectionNames.contains(collectionName)
        if (exists) {
            existsCollection.add(collectionName)
        }
        return exists
    }
}
