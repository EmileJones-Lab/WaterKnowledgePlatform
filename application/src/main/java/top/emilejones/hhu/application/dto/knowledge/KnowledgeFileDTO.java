package top.emilejones.hhu.application.dto.knowledge;

import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.application.dto.mission.OcrMissionDTO;
import top.emilejones.hhu.application.dto.mission.enums.DocumentSplittingMissionType;

import java.time.Instant;
import java.util.List;

public class KnowledgeFileDTO {
    private String embeddingMissionId;
    private DocumentSplittingMissionType type;
    private String fileName;
    private Instant createTime;
    private List<OcrMissionDTO> ocrMission;
    private List<DocumentSplittingMissionDTO> extractStructureMission;
    private List<EmbeddingMissionDTO> embeddingMission;

    public String getEmbeddingMissionId() {
        return embeddingMissionId;
    }

    public void setEmbeddingMissionId(String embeddingMissionId) {
        this.embeddingMissionId = embeddingMissionId;
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
