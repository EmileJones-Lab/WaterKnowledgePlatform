package top.emilejones.hhu.test.model

import org.junit.jupiter.api.Test
import top.emilejones.hhu.model.impl.ModelClientByHttp

class ModelClientByHttpTest {
    private val client = ModelClientByHttp(
        host = "192.168.10.102",
        port = 8666,
        token = "sk-0mECaZXEmFCICxoT8d651d8c3aF44385A1CaC1Df22F23b4b",
        embeddingModel = "gte_embedding",
        rerankModel = "gte_rerank"
    )

    @Test
    fun testEmbedding() {
        val text = """
            <table><tr><td></td><td rowspan="2" colspan="1">序号</td><td rowspan="2" colspan="2">水库名称</td><td rowspan="1" colspan="4">水库防洪能力</td><td rowspan="1" colspan="4">大坝情 况</td><td rowspan="1" colspan="4">溢 洪 道</td></tr><tr><td rowspan="2" colspan="1">(cid:)</td><td rowspan="1" colspan="1">2</td><td rowspan="1" colspan="2">卫星水库</td><td rowspan="1" colspan="1">0.2</td><td rowspan="1" colspan="1">15.5</td><td rowspan="1" colspan="1">164.8</td><td rowspan="1" colspan="1">164.8</td><td rowspan="1" colspan="1">心墙土坝</td><td rowspan="1" colspan="1">165.56</td><td rowspan="1" colspan="1">10</td><td rowspan="1" colspan="1">6.5</td><td rowspan="1" colspan="1">宽顶堰</td><td rowspan="1" colspan="1">164.8</td><td rowspan="1" colspan="1">4.5</td><td rowspan="1" colspan="1">7.2</td></tr></table>
        """.trimIndent()
        println(text.length)
        val embedding = client.embedding(text)
        println(embedding.size)
    }
}