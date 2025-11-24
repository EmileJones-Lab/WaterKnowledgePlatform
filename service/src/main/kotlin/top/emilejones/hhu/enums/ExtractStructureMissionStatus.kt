package top.emilejones.hhu.enums

enum class ExtractStructureMissionStatus(comment: String) {
    RUNNING("运行中"),
    ERROR("任务失败"),
    SUCCESS("运行成功"),
    WAIT_OCR_MISSION("等待OCR任务运行结果")
}