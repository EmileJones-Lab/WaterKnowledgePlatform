package top.emilejones.hhu.application.dto.mission;

import java.util.List;

public class MissionsDTO {
    private String fileId;
    private String fileName;
    private List<OcrMissionDTO> ocrMission;
    private List<DocumentSplittingMissionDTO> extractStructureMission;
    private List<EmbeddingMissionDTO> embeddingMission;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
