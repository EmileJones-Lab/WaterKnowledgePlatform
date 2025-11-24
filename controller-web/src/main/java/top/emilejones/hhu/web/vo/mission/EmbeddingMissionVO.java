package top.emilejones.hhu.web.vo.mission;

import io.swagger.v3.oas.annotations.media.Schema;
import top.emilejones.hhu.enums.EmbeddingMissionType;
import top.emilejones.hhu.enums.EmbeddingMissionStatus;

import java.time.Instant;

@Schema(description = "向量化任务详细信息")
public class EmbeddingMissionVO {
    @Schema(description = "向量化任务唯一Id")
    private String embeddingMissionId;
    @Schema(description = "向量化任务状态")
    private EmbeddingMissionStatus status;
    @Schema(description = "向量化任务类型")
    private EmbeddingMissionType type;
    @Schema(description = "向量化任务失败后详细信息")
    private String remark;
    @Schema(description = "任务开始时间")
    private Instant startTime;
    @Schema(description = "任务结束时间")
    private Instant endTime;

    public EmbeddingMissionType getType() {
        return type;
    }

    public String getEmbeddingMissionId() {
        return embeddingMissionId;
    }

    public void setEmbeddingMissionId(String embeddingMissionId) {
        this.embeddingMissionId = embeddingMissionId;
    }

    public void setType(EmbeddingMissionType type) {
        this.type = type;
    }

    public EmbeddingMissionStatus getStatus() {
        return status;
    }

    public void setStatus(EmbeddingMissionStatus status) {
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
