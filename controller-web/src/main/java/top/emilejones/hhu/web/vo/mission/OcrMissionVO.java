package top.emilejones.hhu.web.vo.mission;

import io.swagger.v3.oas.annotations.media.Schema;
import top.emilejones.hhu.web.vo.mission.enums.MissionStatus;

import java.time.Instant;

@Schema(description = "OCR任务详情")
public class OcrMissionVO {
    @Schema(description = "OCR任务唯一Id")
    private String ocrMissionId;
    @Schema(description = "OCR任务状态")
    private MissionStatus status;
    @Schema(description = "任务失败后详细信息")
    private String remark;
    @Schema(description = "任务开始时间")
    private Instant startTime;
    @Schema(description = "任务结束时间")
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
