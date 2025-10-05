package top.emilejones.hhu.domain.dto

import top.emilejones.hhu.domain.enums.TextType

data class TextNode(
    val text: String,
    var seq: Int,
    val level: Int,
    val type: TextType
) {
    private val childList: MutableList<TextNode> = ArrayList()
    var parentNode: TextNode? = null
    var preNode: TextNode? = null
    var nextNode: TextNode? = null
    var fileNode: FileNode? = null

    fun addChild(childNode: TextNode) {
        childList.add(childNode)
    }

    fun setChild(childNode: TextNode, index: Int) {
        childList.add(index, childNode)
    }

    fun deleteChild(index: Int) {
        childList.removeAt(index)
    }

    fun getChild(index: Int): TextNode {
        return childList[index]
    }

    fun childNum(): Int {
        return childList.size
    }
}
