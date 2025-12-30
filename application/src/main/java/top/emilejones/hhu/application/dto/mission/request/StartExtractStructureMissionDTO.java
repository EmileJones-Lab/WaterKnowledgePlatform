package top.emilejones.hhu.application.dto.mission.request;

import java.util.List;

public class StartExtractStructureMissionDTO {
    private List<String> fileIdList;

    public List<String> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<String> fileIdList) {
        this.fileIdList = fileIdList;
    }
}
