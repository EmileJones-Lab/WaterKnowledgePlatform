package top.emilejones.hhu.model.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service
import top.emilejones.hhu.env.pojo.HttpModelClientConfig
import top.emilejones.hhu.model.ModelClient
import top.emilejones.hhu.model.pojo.RerankResult

@Service
class ModelClientByHttp(
    private val modelClientConfig: HttpModelClientConfig
) : ModelClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun embedding(text: String): List<Float> {
        val url = "http://${modelClientConfig.host}:${modelClientConfig.port}/v1/embeddings"
        val payload = mapOf(
            "model" to modelClientConfig.embeddingModel,
            "input" to text
        )
        val json = gson.toJson(payload)
        val body = json.toRequestBody("application/json".toMediaType())
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
        if (modelClientConfig.token != null)
            requestBuilder.addHeader("Authorization", "Bearer ${modelClientConfig.token}")
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
        // 将召回的结果分批rerank（由于显存问题，需要分批rerank）
        val step = 5
        var index = 0
        val rerankResults: MutableList<RerankResult> = ArrayList<RerankResult>()

        while (index < textList.size) {
            val strings = ArrayList<String>()
            var i = 0
            while (i < step && index + i < textList.size) {
                strings.add(textList[index + i])
                i++
            }
            // 需要将请求的rerank结果的index加一个偏移
            val rerankResult = getRerankResult(query, strings).map { it.copy(index = it.index + index) }.toList()
            rerankResults.addAll(rerankResult)
            index += step
        }

        // 将分批rerank后的结果排序，获取得分最高的maxResultNumber个结果
        return rerankResults.stream()
            .sorted(Comparator.comparingDouble { value: RerankResult ->
                value.score.toDouble()
            }.reversed())
            .toList()
    }

    private fun getRerankResult(query: String, textList: List<String>): List<RerankResult> {
        val url = "http://${modelClientConfig.host}:${modelClientConfig.port}/v1/rerank"
        val payload = mapOf(
            "model" to modelClientConfig.rerankModel,
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

        if (modelClientConfig.token != null)
            requestBuilder.addHeader("Authorization", "Bearer ${modelClientConfig.token}")

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