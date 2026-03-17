package top.emilejones.hhu.application.platform.dto.knowledge;

import top.emilejones.hhu.application.platform.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.platform.dto.mission.enums.DocumentSplittingMissionType;

import java.time.Instant;
import java.util.List;

public class CandidateKnowledgeFileDTO {
    /**
     * 向量化文件id
     */
    private String id;
    /**
     * 向量化文件类型
     */
    private DocumentSplittingMissionType type;
    /**
     * 源文件名称
     */
    private String fileName;
    /**
     * 创建向量化文件的时间
     */
    private Instant createTime;
    /**
     * ocr任务
     */
    private List<OcrMissionDTO> ocrMission;
    /**
     * 结构化提取任务
     */
    private List<DocumentSplittingMissionDTO> extractStructureMission;
    /**
     * 向量化任务
     */
    private List<EmbeddingMissionDTO> embeddingMission;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentSplittingMissionType getType() {
        return type;
    }

    public void setType(DocumentSplittingMissionType type) {
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

    public List<OcrMissionDTO> getOcrMission() {
        return ocrMission;
    }

    public void setOcrMission(List<OcrMissionDTO> ocrMission) {
        this.ocrMission = ocrMission;
    }

    public List<DocumentSplittingMissionDTO> getExtractStructureMission() {
        return extractStructureMission;
    }

    public void setExtractStructureMission(List<DocumentSplittingMissionDTO> extractStructureMission) {
        this.extractStructureMission = extractStructureMission;
    }

    public List<EmbeddingMissionDTO> getEmbeddingMission() {
        return embeddingMission;
    }

    public void setEmbeddingMission(List<EmbeddingMissionDTO> embeddingMission) {
        this.embeddingMission = embeddingMission;
    }
}
