package top.emilejones.hhu.model.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import top.emilejones.hhu.model.ModelClient

class XinferenceHttpClient(
    private val host: String,
    private val port: Int
) : ModelClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val logger = LoggerFactory.getLogger(XinferenceHttpClient::class.java)

    override fun embedding(text: String): List<Float> {
        logger.trace("Embedding text size: [{}]", text.length)
        val url = "http://$host:$port/v1/embeddings"
        val payload = mapOf("model" to "bge-m3", "input" to text)
        val json = gson.toJson(payload)
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        val responseBody = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            response.body?.string() ?: ""
        }

        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = gson.fromJson(responseBody, type)
        val data = map["data"] as? List<Map<String, Any>>
        val embedding = data?.get(0)?.get("embedding") as? List<Double>
        return embedding?.map { it.toFloat() } ?: emptyList()
    }
}