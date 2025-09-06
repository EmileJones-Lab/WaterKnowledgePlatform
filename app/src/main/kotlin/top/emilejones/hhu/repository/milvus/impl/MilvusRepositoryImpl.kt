package top.emilejones.hhu.repository.milvus.impl


import com.google.gson.Gson
import com.google.gson.JsonObject
import io.milvus.v2.client.ConnectConfig
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.DataType
import io.milvus.v2.common.IndexParam
import io.milvus.v2.service.collection.request.AddFieldReq
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.database.request.CreateDatabaseReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.data.BaseVector
import io.milvus.v2.service.vector.request.data.FloatVec
import io.milvus.v2.service.vector.response.SearchResp
import org.slf4j.LoggerFactory
import top.emilejones.hhu.repository.milvus.IMilvusRepository
import top.emilejones.hhu.repository.milvus.po.EmbeddingDatum
import top.emilejones.hhu.repository.neo4j.enums.TextType


class MilvusRepositoryImpl(
    private val host: String,
    private val port: Int,
    private val databaseName: String,
    private val collectionName: String
) : IMilvusRepository {
    private val logger = LoggerFactory.getLogger(MilvusRepositoryImpl::class.java)

    private val client = MilvusClientV2(
        ConnectConfig.builder()
            .uri("http://$host:$port")
            .build()
    )
    private val gson = Gson()

    init {
        val listDatabases = client.listDatabases()
        // 如果不存在database，则创建
        if (!listDatabases.databaseNames.contains(databaseName)) {
            createDatabase()
        }
        val listCollection = client.listCollections()
        // 如果不存在collection，则创建
        if (!listCollection.collectionNames.contains(collectionName)) {
            createCollection()
        }
    }

    override fun searchByVector(queryVector: List<Float>, topK: Int, filter: String?): List<EmbeddingDatum> {
        val mutableListOf: MutableList<BaseVector> = mutableListOf(FloatVec(queryVector))
        val searchParamsMap: Map<String, Any> = mapOf(
            "metric_type" to "L2",
            "params" to mapOf("nprobe" to 10)
        )
        // 构建搜索请求
        val searchReq = SearchReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .annsField("vector")
            .topK(topK)
            .data(mutableListOf)
            .outputFields(listOf("text", "elementId", "vector"))
            .apply {
                if (!filter.isNullOrBlank()) {
                    filter(filter)
                }
            }
            .searchParams(searchParamsMap)
            .build()

        // 发起搜索
        val resp = client.search(searchReq)
        val data = resp.searchResults ?: return emptyList()


        val resultsForQuery: List<SearchResp.SearchResult> = resp.searchResults.firstOrNull() ?: return emptyList()
        // 6. 映射成 EmbeddingDatum
        return resultsForQuery.map { r ->
            val neo4jId = r.entity["elementId"].toString()
            val text = r.entity["text"].toString()
            val vector = r.entity["vector"]
            val type = r.entity["type"].toString()
            if (vector !is List<*>) {
                throw RuntimeException("Illegal vector type")
            }
            EmbeddingDatum(
                vector = vector as List<Float>,
                neo4jElementId = neo4jId,
                text = text,
                type = TextType.valueOf(type)
            )
        }
    }


    override fun insert(datum: EmbeddingDatum): Boolean {
        logger.trace("Start insert embedding node [{}] ", datum.neo4jElementId)
        // 1. 封装向量为 FloatArray
        val vectorArray: FloatArray = datum.vector.toFloatArray()

        // 2. 构建单条数据的 JsonObject
        val jsonData = JsonObject().apply {
            add("vector", gson.toJsonTree(vectorArray))
            addProperty("elementId", datum.neo4jElementId)
            addProperty("text", datum.text)
            addProperty("type", datum.type.name)
        }

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

    override fun batchInsert(data: List<EmbeddingDatum>): Boolean {
        logger.trace("Start batch insert embedding nodes, nodes number: [{}]", data.size)
        val jsonObjectList = data.map { datum ->
            // 封装向量为 FloatArray
            val vectorArray: FloatArray = datum.vector.toFloatArray()

            // 构建单条数据的 JsonObject
            JsonObject().apply {
                add("vector", gson.toJsonTree(vectorArray))
                addProperty("elementId", datum.neo4jElementId)
                addProperty("text", datum.text)
                addProperty("type", datum.type.name)
            }
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

    override fun close() {
        client.close()
    }

    private fun createDatabase() {
        val createDatabaseReq = CreateDatabaseReq.builder()
            .databaseName(databaseName)
            .build()

        client.createDatabase(createDatabaseReq)
        logger.debug("Create milvus database named [{}]", databaseName)
    }

    private fun createCollection() {
        // 1. 创建schema
        val schema = MilvusClientV2.CreateSchema()
        // 2. 创建字段
        // 2.1 elementId (VarChar 主键)
        schema.addField(
            AddFieldReq.builder()
                .fieldName("elementId")
                .dataType(DataType.VarChar)
                .isPrimaryKey(true)
                .autoID(false)
                .maxLength(64) // 必须指定长度
                .build()
        )

        // 2.2 text (VarChar 存储原始文本)
        schema.addField(
            AddFieldReq.builder()
                .fieldName("text")
                .dataType(DataType.VarChar)
                .maxLength(4096)
                .build()
        )

        // 2.3 vector (FloatVector，存储向量)
        schema.addField(
            AddFieldReq.builder()
                .fieldName("vector")
                .dataType(DataType.FloatVector)
                .dimension(1024) // 修改成你 embedding 的维度
                .build()
        )

        // 2.4 type (VarChar 存储原始文本)
        schema.addField(
            AddFieldReq.builder()
                .fieldName("type")
                .dataType(DataType.VarChar)
                .maxLength(20)
                .build()
        )
        // 3. 索引
        val indexParams = listOf(
            IndexParam.builder()
                .fieldName("vector")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build()
        )

        // 4. 创建 Collection
        val createReq = CreateCollectionReq.builder()
            .collectionSchema(schema)
            .collectionName(collectionName)
            .indexParams(indexParams)
            .databaseName(databaseName)
            .build()

        client.createCollection(createReq)
        logger.debug("Create milvus collection named [{}]", collectionName)
    }
}
