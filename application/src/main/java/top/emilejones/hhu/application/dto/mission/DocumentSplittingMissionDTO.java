package top.emilejones.hhu.application.dto.mission;

import top.emilejones.hhu.application.dto.mission.enums.DocumentSplittingMissionType;
import top.emilejones.hhu.application.dto.mission.enums.MissionStatus;

import java.time.Instant;

public class DocumentSplittingMissionDTO {
    private String extractStructureMissionId;
    private MissionStatus status;
    private DocumentSplittingMissionType type;
    private String remark;
    private Instant startTime;
    private Instant endTime;

    public String getExtractStructureMissionId() {
        return extractStructureMissionId;
    }

    public void setExtractStructureMissionId(String extractStructureMissionId) {
        this.extractStructureMissionId = extractStructureMissionId;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public DocumentSplittingMissionType getType() {
        return type;
    }

    public void setType(DocumentSplittingMissionType type) {
        this.type = type;
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
