package top.emilejones.hhu.web.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import top.emilejones.hhu.enums.EmbeddingMissionType;
import top.emilejones.hhu.web.vo.mission.EmbeddingMissionVO;
import top.emilejones.hhu.web.vo.mission.ExtractStructureMissionVO;
import top.emilejones.hhu.web.vo.mission.OcrMissionVO;

import java.time.Instant;
import java.util.List;

@Schema(description = "知识库中的文件的元信息")
public class KnowledgeFileVO {
    @Schema(description = "向量化任务唯一Id")
    private String embeddingMissionId;
    @Schema(description = "向量化任务类型")
    private EmbeddingMissionType type;
    @Schema(description = "文件名称")
    private String fileName;
    @Schema(description = "加入知识库时间")
    private Instant createTime;
    @Schema(description = "OCR任务列表（按照时间倒序排列），如果没有OCR任务，则返回空列表")
    private List<OcrMissionVO> ocrMission;
    @Schema(description = "结构提取任务列表（按照时间倒序排列），如果没有结构提取任务，则返回空列表")
    private List<ExtractStructureMissionVO> extractStructureMission;
    @Schema(description = "向量化任务列表（按照时间倒序排列），如果没有向量化任务，则返回空列表")
    private List<EmbeddingMissionVO> embeddingMission;

    public String getEmbeddingMissionId() {
        return embeddingMissionId;
    }

    public void setEmbeddingMissionId(String embeddingMissionId) {
        this.embeddingMissionId = embeddingMissionId;
    }

    public List<OcrMissionVO> getOcrMission() {
        return ocrMission;
    }

    public void setOcrMission(List<OcrMissionVO> ocrMission) {
        this.ocrMission = ocrMission;
    }

    public List<ExtractStructureMissionVO> getExtractStructureMission() {
        return extractStructureMission;
    }

    public void setExtractStructureMission(List<ExtractStructureMissionVO> extractStructureMission) {
        this.extractStructureMission = extractStructureMission;
    }

    public List<EmbeddingMissionVO> getEmbeddingMission() {
        return embeddingMission;
    }

    public void setEmbeddingMission(List<EmbeddingMissionVO> embeddingMission) {
        this.embeddingMission = embeddingMission;
    }

    public EmbeddingMissionType getType() {
        return type;
    }

    public void setType(EmbeddingMissionType type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }
}
