package top.emilejones.hhu.web.vo.mission;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "针对文件的相关任务详细信息")
public class MissionsVO {
    @Schema(description = "文件的唯一Id")
    private String fileId;
    @Schema(description = "文件名称")
    private String fileName;
    @Schema(description = "OCR任务列表（按照时间倒序排列），如果没有OCR任务，则返回空列表")
    private List<OcrMissionVO> ocrMission;
    @Schema(description = "结构提取任务列表（按照时间倒序排列），如果没有结构提取任务，则返回空列表")
    private List<ExtractStructureMissionVO> extractStructureMission;
    @Schema(description = "向量化任务列表（按照时间倒序排列），如果没有向量化任务，则返回空列表")
    private List<EmbeddingMissionVO> embeddingMission;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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
}
