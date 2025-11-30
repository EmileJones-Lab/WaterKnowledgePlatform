package top.emilejones.hhu.domain.pipeline

enum class MissionStatus(val comment: String) {
    CREATED("创建成功"),
    RUNNING("运行中"),
    ERROR("任务失败"),
    SUCCESS("任务成功"),
    PENDING("等待中")
}