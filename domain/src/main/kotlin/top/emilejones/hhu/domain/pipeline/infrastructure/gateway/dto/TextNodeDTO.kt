package top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto

import top.emilejones.hhu.domain.pipeline.TextType


data class TextNodeDTO(
    val id: String,
    val text: String,
    var seq: Int,
    val level: Int,
    val type: TextType
) {
    private val childList: MutableList<TextNodeDTO> = ArrayList()
    var parentNode: TextNodeDTO? = null
    var preNode: TextNodeDTO? = null
    var nextNode: TextNodeDTO? = null
    var fileNode: FileNodeDTO? = null

    fun addChild(childNode: TextNodeDTO) {
        childList.add(childNode)
    }

    fun setChild(childNode: TextNodeDTO, index: Int) {
        childList.add(index, childNode)
    }

    fun deleteChild(index: Int) {
        childList.removeAt(index)
    }

    fun getChild(index: Int): TextNodeDTO {
        return childList[index]
    }

    fun childNum(): Int {
        return childList.size
    }
}
