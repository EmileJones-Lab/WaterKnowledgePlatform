package top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto

/**
 * 文件节点数据传输对象，作为文档树的根节点载体。
 *
 * - `id`：文件节点唯一标识。
 * - `fileId`：业务侧文件唯一标识；支持延迟绑定，且只允许写入一次。
 * - 子节点集合：该文件下的所有文本节点。
 */
class FileNodeDTO(
    val id: String,
    fileId: String
) {
    var fileId: String = fileId
        set(fileId: String) {
            require(field.isBlank()) { "FileNodeDTO已经隶属于文件[$fileId]" }
            field = fileId
        }

    private val childList: MutableList<TextNodeDTO> = ArrayList()

    /** 将文本子节点追加到末尾。 */
    fun addChild(childNode: TextNodeDTO) {
        childList.add(childNode)
    }

    /** 按下标获取文本子节点。 */
    fun getChild(index: Int): TextNodeDTO {
        return childList[index]
    }

    /** 文本子节点数量。 */
    fun childNum(): Int {
        return childList.size
    }
}
