package top.emilejones.hhu.qa.converter.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.slf4j.LoggerFactory
import top.emilejones.hhu.qa.converter.QAConverter
import top.emilejones.hhu.qa.entity.EventData
import top.emilejones.hhu.qa.entity.QAPair
import java.util.concurrent.TimeUnit

class ConcurrentDifyApiConverter(
    private val url: String,
    private val apiKey: String,
) : QAConverter {
    companion object {
        private val logger = LoggerFactory.getLogger(ConcurrentDifyApiConverter::class.java)
        private val gson = Gson()
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client =
        OkHttpClient.Builder().readTimeout(100, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()

    /**
     * 将文本转换为QA对，此方法会自动切分字符串，每一段不超过指定的最大长度
     * @param text 需要转换为QA对的文本，不建议过长，建议传入一行文本
     */
    override suspend fun convert(text: String): Result<List<QAPair>> {
        // 开始生成QA对
        val response = scope.async {
            convertToQA(text)
        }.await()

        return response.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(RuntimeException()) }
        )
    }

    /**
     * 将文本转换为QA对
     * @param text 需要转换为QA对的文本
     */
    private fun convertToQA(text: String): Result<List<QAPair>> {
        val jsonText = text.escapeJsonContent()
        val json = """
                    {
                        "inputs": {
                            "input": "$jsonText"
                        },
                        "response_mode": "streaming",
                        "user": "kotlin"
                    }
                    """.trimIndent()

        val request = Request.Builder().url(url)
            .addHeader("Authorization", "Bearer $apiKey").addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType())).build()

        var qaList: List<QAPair> = emptyList()

        logger.info("start convert: \"$jsonText\"")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                logger.error("requestSize: ${jsonText.length}, message: ${response.message}, text: $jsonText")
                return Result.failure(IOException("Unexpected code $response"))
            }

            val source = response.body?.source() ?: throw RuntimeException("The response is not a sse response")

            while (!source.exhausted()) {
                val line = source.readUtf8Line()!!.trim()
                if (line.isBlank() || !line.startsWith("data:")) continue
                logger.debug(line)
                val eventData = gson.fromJson(line.substring(6), EventData::class.java)
                if ("workflow_finished" == eventData.event && eventData?.data?.outputs?.qaList != null) {
                    qaList = gson.fromJson(
                        eventData.data.outputs.qaList, object : TypeToken<List<QAPair>>() {})
                }

            }
        }

        return Result.success(qaList)
    }
}

/**
 * 将字符串变为JSON安全的字符串，可以放在`""`中
 */
private fun String.escapeJsonContent(): String {
    val sb = StringBuilder()
    for (ch in this) {
        when (ch) {
            '\\' -> sb.append("\\\\")
            '\"' -> sb.append("\\\"")
            '\b' -> sb.append("\\b")
            '\u000C' -> sb.append("\\f")  // form feed
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> {
                if (ch < ' ') {
                    sb.append(String.format("\\u%04x", ch.code))
                } else {
                    sb.append(ch)
                }
            }
        }
    }
    return sb.toString()
}