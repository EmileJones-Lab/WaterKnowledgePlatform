package top.emilejones.hhu.domain.pipeline

class EmbeddingRecord(
    val vector: List<Float>,
    val textNodeElementId: String,
    val text: String,
    val type: TextType
) {
}
