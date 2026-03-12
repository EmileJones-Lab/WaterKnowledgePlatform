package top.emilejones.hhu.textsplitter.ocr

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.source
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import top.emilejones.hhu.common.env.pojo.MinerUConfig
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.MinerUImage
import top.emilejones.hhu.domain.pipeline.infrastructure.dto.MinerUMarkdownFile
import java.io.IOException
import java.io.InputStream
import java.util.*
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
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    /**
     * Send the given file stream to MinerU for OCR and return the processed content as a stream.
     */
    fun ocr(input: InputStream): MinerUMarkdownFile {
        val url = "http://${minerUConfig.host}:${minerUConfig.port}/file_parse"
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
            .addFormDataPart("files", "upload", fileBody)
            .addFormDataPart("return_md", "true")
            .addFormDataPart("response_format_zip", "false")
            .addFormDataPart("formula_enable", "true")
            .addFormDataPart("table_enable", "true")
            .addFormDataPart("return_images", "true")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val message = "Failed to call MinerU OCR, status=${response.code}"
                logger.error(response.body?.string(), message)
                throw IOException(message, null)
            }
            val body = response.body ?: throw IOException("MinerU OCR response body is null", null)
            val bytes = body.bytes()
            val contentType = body.contentType()

            // MinerU returns JSON when return_md=true, extract markdown and images if present
            parseMarkdownResponse(bytes, contentType)
        }

    }

    private fun parseMarkdownResponse(bodyBytes: ByteArray, mediaType: MediaType?): MinerUMarkdownFile {
        val bodyAsString = bodyBytes.toString(Charsets.UTF_8)
        val looksLikeJson = mediaType?.subtype?.contains("json", ignoreCase = true) == true ||
                bodyAsString.trimStart().startsWith("{")

        if (!looksLikeJson) {
            return MinerUMarkdownFile(markdownContent = bodyAsString, images = emptyList())
        }

        val jsonElement = JsonParser.parseString(bodyAsString)
        parseStructuredPayload(jsonElement)?.let { return it }

        val fallbackMarkdown = extractMarkdownFromJson(jsonElement)
            ?: bodyAsString
        return MinerUMarkdownFile(markdownContent = fallbackMarkdown, images = emptyList())
    }

    private fun parseStructuredPayload(element: JsonElement): MinerUMarkdownFile? {
        if (!element.isJsonObject) return null
        val root = element.asJsonObject
        val upload = root["results"]?.asJsonObject
            ?.get("upload")?.asJsonObject ?: return null

        val markdown = upload["md_content"]?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
            ?: return null

        val imagesJson = upload["images"]?.takeIf { it.isJsonObject }?.asJsonObject
        val images = mutableListOf<MinerUImage>()
        imagesJson?.entrySet()?.forEach { (name, value) ->
            val raw = value.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString ?: return@forEach
            val decoded = decodeDataUri(raw)
            if (decoded != null) {
                images += MinerUImage(
                    imageName = name,
                    contentType = decoded.first,
                    data = decoded.second,
                    relativePath = "images/$name"
                )
            }
        }
        return MinerUMarkdownFile(markdownContent = markdown, images = images)
    }

    private fun extractMarkdownFromJson(element: JsonElement): String? {
        return when {
            element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString
            element.isJsonArray -> {
                val joined = element.asJsonArray.mapNotNull { extractMarkdownFromJson(it) }
                if (joined.isNotEmpty()) joined.joinToString("\n") else null
            }

            element.isJsonObject -> {
                val obj = element.asJsonObject
                // prefer fields named markdown/md/content_list first, then search recursively
                val preferredKeys = listOf("markdown", "md", "md_list", "content_list", "content", "data", "result")
                for (key in preferredKeys) {
                    if (obj.has(key)) {
                        extractMarkdownFromJson(obj.get(key))?.let { return it }
                    }
                }
                // fallback: search any child
                searchAnyString(obj)
            }

            else -> null
        }
    }

    private fun decodeDataUri(raw: String): Pair<String?, ByteArray>? {
        val commaIndex = raw.indexOf(',')
        if (commaIndex <= 0) return null
        val header = raw.substring(0, commaIndex)
        val dataPart = raw.substring(commaIndex + 1)
        val contentType = header.substringAfter("data:", "").substringBefore(";base64").ifEmpty { null }
        return runCatching {
            val decoded = Base64.getDecoder().decode(dataPart)
            contentType to decoded
        }.getOrNull()
    }

    private fun searchAnyString(obj: JsonObject): String? {
        obj.entrySet().forEach { (_, value) ->
            extractMarkdownFromJson(value)?.let { return it }
        }
        return null
    }
}
