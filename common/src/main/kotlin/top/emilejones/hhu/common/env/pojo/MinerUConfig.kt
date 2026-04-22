package top.emilejones.hhu.common.env.pojo

import kotlinx.serialization.Serializable

@Serializable
data class MinerUConfig(
    val host: String,
    val port: Int,
    val connectTimeout: Long = 60,
    val writeTimeout: Long = 120,
    val readTimeout: Long = 0,
    val outputDir: String = "./output",
    val langList: List<String> = listOf("ch"),
    val backend: String = "pipeline",
    val parseMethod: String = "auto",
    val formulaEnable: Boolean = true,
    val tableEnable: Boolean = true,
    val returnMd: Boolean = true,
    val returnMiddleJson: Boolean = false,
    val returnModelOutput: Boolean = false,
    val returnContentList: Boolean = true,
    val returnImages: Boolean = true,
    val responseFormatZip: Boolean = false,
    val startPageId: Int = 0,
    val endPageId: Int = 99999
)
