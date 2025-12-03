package top.emilejones.hhu.domain.pipeline.infrastructure.gateway.dto

class FileNodeDTO(
    val id: String,
    fileId: String?
) {
    var fileId: String? = fileId
        set(fileId: String?) {
            if (fileId == null) return
            require(this.fileId == null) { "FileNodeDTO已经隶属于文件[$fileId]" }
            field = fileId
        }

    private val childList: MutableList<TextNodeDTO> = ArrayList()

    fun addChild(childNode: TextNodeDTO) {
        childList.add(childNode)
    }

    fun getChild(index: Int): TextNodeDTO {
        return childList[index]
    }

    fun childNum(): Int {
        return childList.size
    }
}