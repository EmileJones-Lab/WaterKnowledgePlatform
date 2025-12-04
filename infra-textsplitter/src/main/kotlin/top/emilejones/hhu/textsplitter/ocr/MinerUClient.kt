package top.emilejones.hhu.textsplitter.ocr

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import top.emilejones.hhu.env.pojo.MinerUConfig
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * HTTP client for calling MinerU OCR service.
 */
@Component
class MinerUClient(
    private val minerUConfig: MinerUConfig
) {
    private val logger = LoggerFactory.getLogger(MinerUClient::class.java)
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Send the given file stream to MinerU for OCR and return the processed content as a stream.
     */
    fun ocr(input: InputStream): InputStream {
        val url = "http://${minerUConfig.host}:${minerUConfig.port}/ocr"
        val fileBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                input.use { source ->
                    sink.writeAll(source.source())
                }
            }
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "upload", fileBody)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val message = "Failed to call MinerU OCR, status=${response.code}"
                logger.warn(message)
                throw IOException(message, null)
            }
            val body = response.body ?: throw IOException("MinerU OCR response body is null", null)
            val bytes = body.bytes()
            ByteArrayInputStream(bytes)
        }

    }
}
