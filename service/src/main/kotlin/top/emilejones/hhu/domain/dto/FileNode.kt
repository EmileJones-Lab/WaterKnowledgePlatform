package top.emilejones.hhu.domain.dto

data class FileNode (
    val fileName: String
) {
    private val childList: MutableList<TextNode> = ArrayList()

    fun addChild(childNode: TextNode) {
        childList.add(childNode)
    }

    fun getChild(index: Int): TextNode {
        return childList[index]
    }

    fun childNum(): Int {
        return childList.size
    }
}