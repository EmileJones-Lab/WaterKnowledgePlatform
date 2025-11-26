package top.emilejones.hhu.domain.structuresplitter

class EmbeddingRecord(
    val vector: List<Float>,
    val textNodeElementId: String,
    val text: String,
    val type: TextType
) {
}
