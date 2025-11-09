package top.emilejones.hhu.domain.dto

import top.emilejones.hhu.domain.enums.TextType

/**
 * 向量数据库中的一条数据
 *
 * @author EmileJones
 */
data class DenseRecallResult(
    val elementId: String,
    val text: String,
    val type: TextType,
    val score: Float
)
