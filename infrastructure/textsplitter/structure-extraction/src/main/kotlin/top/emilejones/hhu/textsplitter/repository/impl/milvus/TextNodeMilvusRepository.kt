package top.emilejones.hhu.textsplitter.repository.impl.milvus

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.milvus.v2.client.MilvusClientV2
import io.milvus.v2.common.DataType
import io.milvus.v2.common.IndexParam
import io.milvus.v2.service.collection.request.AddFieldReq
import io.milvus.v2.service.collection.request.CreateCollectionReq
import io.milvus.v2.service.collection.request.DropCollectionReq
import io.milvus.v2.service.collection.request.HasCollectionReq
import io.milvus.v2.service.database.request.CreateDatabaseReq
import io.milvus.v2.service.vector.request.InsertReq
import io.milvus.v2.service.vector.request.QueryReq
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.UpsertReq
import io.milvus.v2.service.vector.request.data.FloatVec
import io.milvus.v2.service.vector.response.SearchResp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import top.emilejones.hhu.textsplitter.domain.po.milvus.TextNodeEmbeddingDatum
import top.emilejones.hhu.infrastructure.configuration.env.pojo.OpenAiConfig
import top.emilejones.hhu.infrastructure.configuration.env.pojo.MilvusConfig
import top.emilejones.hhu.textsplitter.repository.ITextNodeMilvusRepository


@Service
class TextNodeMilvusRepository(
    private val client: MilvusClientV2,
    milvusConfig: MilvusConfig,
    openAiConfig: OpenAiConfig
) : ITextNodeMilvusRepository {
    private val databaseName: String = milvusConfig.database
    private val dimension: Int = openAiConfig.dimension
    private val existsCollection: MutableSet<String> = HashSet()
    private val logger = LoggerFactory.getLogger(TextNodeMilvusRepository::class.java)
    private val gson = Gson()

    init {
        createDatabaseIfNotExists()
        client.useDatabase(databaseName)
    }

    override fun insert(collectionName: String, datum: TextNodeEmbeddingDatum): Boolean {
        checkCollectionExistOrCreate(collectionName)
        logger.trace("Start insert embedding node [{}] ", datum.neo4jNodeId)
        val jsonData = datum.toJsonObject(datum.vector.toFloatArray())

        val insertReq = InsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(listOf(jsonData))
            .build()

        val resp = client.insert(insertReq)
        logger.trace("Success insert embedding node, primary key is [{}] ", resp.primaryKeys[0])
        return true
    }

    override fun batchInsert(collectionName: String, data: List<TextNodeEmbeddingDatum>): Boolean {
        checkCollectionExistOrCreate(collectionName)
        logger.trace("Start batch insert embedding nodes, nodes number: [{}]", data.size)
        val jsonObjectList = data.map { it.toJsonObject(it.vector.toFloatArray()) }.toMutableList()

        val insertReq = InsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(jsonObjectList)
            .build()

        val resp = client.insert(insertReq)
        logger.trace("Success batch insert embedding nodes, nodes number: [{}] ", resp.insertCnt)
        return true
    }

    override fun batchDeleteByFileNodeIds(collectionName: String, fileNodeIds: List<String>): Boolean {
        if (fileNodeIds.isEmpty()) return true
        if (!ensureCollectionExists(collectionName)) return false
        
        val fileIdsString = fileNodeIds.joinToString("\", \"", "\"", "\"")
        val filter = "fileNodeId in [$fileIdsString]"

        val queryReq = QueryReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .filter(filter)
            .outputFields(listOf("neo4jNodeId", "fileNodeId", "vector", "isDelete"))
            .build()
        val queryResults = client.query(queryReq).queryResults ?: emptyList()
        if (queryResults.isEmpty()) return true

        val tombstones = queryResults.mapNotNull { result ->
            mapToEmbeddingDatum(result.entity, overrideIsDelete = true)
        }
        if (tombstones.isEmpty()) return false

        val upsertReq = UpsertReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .data(tombstones.map { it.toJsonObject(it.vector.toFloatArray()) })
            .build()
        client.upsert(upsertReq)
        return true
    }

    override fun searchByVector(
        collectionName: String,
        queryVector: List<Float>,
        topK: Int,
        filter: String?
    ): List<TextNodeEmbeddingDatum> {
        val searchReq = SearchReq.builder()
            .databaseName(databaseName)
            .collectionName(collectionName)
            .annsField("vector")
            .topK(topK)
            .data(listOf(FloatVec(queryVector)))
            .filter(buildFilter(filter))
            .outputFields(listOf("neo4jNodeId", "fileNodeId", "vector", "isDelete"))
            .searchParams(mapOf("metric_type" to "COSINE", "params" to gson.toJson(mapOf("nprobe" to 10))))
            .build()

        val resp = client.search(searchReq)
        val data = resp.searchResults ?: return emptyList()
        val resultsForQuery: List<SearchResp.SearchResult> = data.firstOrNull() ?: return emptyList()
        return resultsForQuery.mapNotNull { mapToEmbeddingDatum(it.entity) }
    }

    override fun dropCollection(collectionName: String) {
        client.dropCollection(DropCollectionReq.builder().collectionName(collectionName).databaseName(databaseName).build())
        existsCollection.remove(collectionName)
    }

    private fun checkCollectionExistOrCreate(collectionName: String) {
        if (existsCollection.contains(collectionName)) return
        val hasReq = HasCollectionReq.builder().databaseName(databaseName).collectionName(collectionName).build()
        if (!client.hasCollection(hasReq)) createCollection(collectionName)
        existsCollection.add(collectionName)
    }

    private fun createDatabaseIfNotExists() {
        val resp = client.listDatabases()
        if (resp.databaseNames.contains(databaseName)) return
        client.createDatabase(CreateDatabaseReq.builder().databaseName(databaseName).build())
    }

    override fun createCollection(collectionName: String) {
        val hasReq = HasCollectionReq.builder().databaseName(databaseName).collectionName(collectionName).build()
        if (client.hasCollection(hasReq)) return

        val schema = MilvusClientV2.CreateSchema()
        schema.addField(AddFieldReq.builder().fieldName("neo4jNodeId").dataType(DataType.VarChar).isPrimaryKey(true).autoID(false).maxLength(36).build())
        schema.addField(AddFieldReq.builder().fieldName("fileNodeId").dataType(DataType.VarChar).maxLength(128).build())
        schema.addField(AddFieldReq.builder().fieldName("vector").dataType(DataType.FloatVector).dimension(dimension).build())
        schema.addField(AddFieldReq.builder().fieldName("isDelete").dataType(DataType.Bool).defaultValue(false).enableDefaultValue(true).build())
        
        val indexParams = listOf(IndexParam.builder().fieldName("vector").indexType(IndexParam.IndexType.AUTOINDEX).metricType(IndexParam.MetricType.COSINE).build())
        val createReq = CreateCollectionReq.builder().collectionSchema(schema).collectionName(collectionName).indexParams(indexParams).databaseName(databaseName).build()
        client.createCollection(createReq)
    }

    private fun buildFilter(filter: String?): String {
        val base = "isDelete == false"
        return if (filter.isNullOrBlank()) base else "$base && ($filter)"
    }

    private fun mapToEmbeddingDatum(entity: Map<String, Any?>, overrideIsDelete: Boolean? = null): TextNodeEmbeddingDatum? {
        val vectorValue = entity["vector"] ?: return null
        val vector = convertVector(vectorValue) ?: return null
        val neo4jId = entity["neo4jNodeId"]?.toString() ?: return null
        val fileNodeId = entity["fileNodeId"]?.toString() ?: ""
        val isDelete = overrideIsDelete ?: (entity["isDelete"] as? Boolean ?: false)
        return TextNodeEmbeddingDatum(vector = vector, neo4jNodeId = neo4jId, fileNodeId = fileNodeId, isDelete = isDelete)
    }

    private fun convertVector(vector: Any): List<Float>? {
        val source: List<*> = when (vector) {
            is List<*> -> vector
            is FloatArray -> vector.toList()
            is DoubleArray -> vector.map { it }
            else -> return null
        }
        return source.mapNotNull { (it as? Number)?.toFloat() }
    }

    private fun TextNodeEmbeddingDatum.toJsonObject(vectorArray: FloatArray): JsonObject {
        return JsonObject().apply {
            add("vector", gson.toJsonTree(vectorArray))
            addProperty("neo4jNodeId", neo4jNodeId)
            addProperty("fileNodeId", fileNodeId)
            addProperty("isDelete", isDelete)
        }
    }

    private fun ensureCollectionExists(collectionName: String): Boolean {
        if (existsCollection.contains(collectionName)) return true
        val exists = client.hasCollection(HasCollectionReq.builder().databaseName(databaseName).collectionName(collectionName).build())
        if (exists) existsCollection.add(collectionName)
        return exists
    }
}
