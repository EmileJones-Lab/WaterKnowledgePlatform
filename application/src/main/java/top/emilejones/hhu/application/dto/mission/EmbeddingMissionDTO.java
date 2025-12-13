package top.emilejones.hhu.application.dto.mission;

import top.emilejones.hhu.application.dto.mission.enums.MissionStatus;

import java.time.Instant;

public class EmbeddingMissionDTO {
    private String embeddingMissionId;
    private MissionStatus status;
    private String remark;
    private Instant startTime;
    private Instant endTime;

    public String getEmbeddingMissionId() {
        return embeddingMissionId;
    }

    public void setEmbeddingMissionId(String embeddingMissionId) {
        this.embeddingMissionId = embeddingMissionId;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
}
