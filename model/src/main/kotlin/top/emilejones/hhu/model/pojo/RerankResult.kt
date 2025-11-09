package top.emilejones.hhu.model.pojo

data class RerankResult(
    // 此片段在输入的文档列表中的下标
    val index: Int,
    // 此片段原文
    val text: String,
    // 分数
    val score: Float
)
