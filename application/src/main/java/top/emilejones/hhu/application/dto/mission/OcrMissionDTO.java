package top.emilejones.hhu.application.dto.mission;

import top.emilejones.hhu.application.dto.mission.enums.MissionStatus;

import java.time.Instant;

public class OcrMissionDTO {
    private String ocrMissionId;
    private MissionStatus status;
    private String remark;
    private Instant createTime;
    private Instant startTime;
    private Instant endTime;

    public String getOcrMissionId() {
        return ocrMissionId;
    }

    public void setOcrMissionId(String ocrMissionId) {
        this.ocrMissionId = ocrMissionId;
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

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
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
