package top.emilejones.hhu.application.platform.dto.mission.request;

import java.util.List;

public class StartEmbeddingMissionDTO {
    private List<String> fileIdList;

    public List<String> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<String> fileIdList) {
        this.fileIdList = fileIdList;
    }
}
