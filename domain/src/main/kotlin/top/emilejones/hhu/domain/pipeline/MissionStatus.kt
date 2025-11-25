package top.emilejones.hhu.domain.pipeline

enum class MissionStatus(comment: String) {
    RUNNING("运行中"),
    ERROR("任务失败"),
    SUCCESS("运行成功"),
    PENDING("等待中")
}