package top.emilejones.hhu.model.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.model.pojo.RerankResult

class ModelClientByHttp(
    private val host: String,
    private val port: Int,
    private val token: String?,
    private val embeddingModel: String,
    private val rerankModel: String
) : ModelClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun embedding(text: String): List<Float> {
        val url = "http://$host:$port/v1/embeddings"
        val payload = mapOf(
            "model" to embeddingModel,
            "input" to text
        )
        val json = gson.toJson(payload)
        val body = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
        if (token != null)
            requestBuilder.addHeader("Authorization", "Bearer $token")
        val request = requestBuilder.build()

        val responseBody = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected response code, response: [$response]")
            response.body?.string() ?: ""
        }

        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(responseBody, type)
        val data = map["data"] as? List<Map<String, Any>>
        val embedding = data?.get(0)?.get("embedding") as? List<Double>
        return embedding?.map { it.toFloat() } ?: emptyList()
    }

    override fun rerank(query: String, textList: List<String>): List<RerankResult> {
        val url = "http://$host:$port/v1/rerank"
        val payload = mapOf(
            "model" to rerankModel,
            "query" to query,
            "documents" to textList
        )
        val json = gson.toJson(payload)
        val body = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")

        if (token != null)
            requestBuilder.addHeader("Authorization", "Bearer $token")

        val request = requestBuilder.build()

        val responseBody = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            response.body?.string() ?: ""
        }

        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(responseBody, type)

        // 解析 results 并取出 index 列表
        val results = map["results"] as? List<Map<String, Any>> ?: emptyList()

        val selected = results.map {
            val idx = (it["index"] as? Number)!!.toInt()
            val score = (it["relevance_score"] as? Number)!!.toFloat()
            RerankResult(
                index = idx,
                text = textList[idx],
                score = score
            )
        }

        return selected
    }
}