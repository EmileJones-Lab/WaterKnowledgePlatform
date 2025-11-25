package top.emilejones.hhu.web.vo.mission;

import io.swagger.v3.oas.annotations.media.Schema;
import top.emilejones.hhu.web.vo.mission.enums.DocumentSplittingMissionType;
import top.emilejones.hhu.web.vo.mission.enums.MissionStatus;

import java.time.Instant;

@Schema(description = "结构提取任务详细信息")
public class DocumentSplittingMissionVO {
    @Schema(description = "结构提取任务唯一Id")
    private String extractStructureMissionId;
    @Schema(description = "结构提取任务状态")
    private MissionStatus status;
    @Schema(description = "文本切割类型")
    private DocumentSplittingMissionType type;
    @Schema(description = "结构任务提取失败后详细说明")
    private String remark;
    @Schema(description = "任务开始时间")
    private Instant startTime;
    @Schema(description = "任务结束时间")
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

    public DocumentSplittingMissionType getType() {
        return type;
    }

    public void setType(DocumentSplittingMissionType type) {
        this.type = type;
    }
}
