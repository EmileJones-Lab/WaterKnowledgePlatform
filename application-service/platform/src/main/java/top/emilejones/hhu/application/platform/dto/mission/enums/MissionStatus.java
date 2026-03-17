package top.emilejones.hhu.application.platform.dto.mission.enums;

public enum MissionStatus {
    RUNNING("运行中"),
    ERROR("任务失败"),
    SUCCESS("运行成功"),
    PENDING("等待中");

    private final String comment;

    MissionStatus(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
