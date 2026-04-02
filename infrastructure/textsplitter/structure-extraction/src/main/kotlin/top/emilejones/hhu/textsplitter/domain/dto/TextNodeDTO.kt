package top.emilejones.hhu.textsplitter.domain.dto

import top.emilejones.hhu.domain.result.TextType

/**
 * 文本节点数据传输对象，描述树状结构中的单个节点。
 *
 * - `id`：节点唯一标识。
 * - `text`：节点文本内容，建议为原始抽取内容。
 * - `seq`：兄弟节点中的顺序（从 0 开始，具体由调用方约定）。
 * - `level`：层级深度，根节点为 0，文本节点为 INT_MAX。
 * - `type`：节点类型，见 `TextType`。
 *
 * 额外关联：
 * - `parentNode`：父节点；NULL 根节点的父为 null。
 * - `preNode` / `nextNode`：同层前驱、后继节点。
 * - `fileNode`：所属文件节点，可用于回填 fileId。
 */
data class TextNodeDTO(
    val id: String,
    var text: String,
    var seq: Int,
    var level: Int,
    var type: TextType,
    var summary: String? = null
) {
    private val childList: MutableList<TextNodeDTO> = ArrayList()
    var parentNode: TextNodeDTO? = null
    var preNode: TextNodeDTO? = null
    var nextNode: TextNodeDTO? = null
    var fileNode: FileNodeDTO? = null

    /** 将子节点追加到末尾。 */
    fun addChild(childNode: TextNodeDTO) {
        childList.add(childNode)
    }

    /** 在指定位置插入子节点，原有元素顺延。 */
    fun setChild(childNode: TextNodeDTO, index: Int) {
        childList.add(index, childNode)
    }

    /** 按下标移除子节点。 */
    fun deleteChild(index: Int) {
        childList.removeAt(index)
    }

    /** 获取指定下标的子节点。 */
    fun getChild(index: Int): TextNodeDTO {
        return childList[index]
    }

    /** 子节点数量。 */
    fun childNum(): Int {
        return childList.size
    }
}
