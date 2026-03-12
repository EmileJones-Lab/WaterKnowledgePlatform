package top.emilejones.hhu.domain.result

/**
 * 任务状态机的枚举定义。
 * @author EmileJones
 */
enum class MissionStatus(val comment: String) {
    CREATED("创建成功"),
    RUNNING("运行中"),
    ERROR("任务失败"),
    SUCCESS("任务成功"),
    PENDING("等待中")
}
