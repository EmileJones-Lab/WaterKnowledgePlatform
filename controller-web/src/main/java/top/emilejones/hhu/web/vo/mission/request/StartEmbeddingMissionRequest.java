package top.emilejones.hhu.web.vo.mission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "请求开启一个层次结构向量化任务的请求体")
public class StartEmbeddingMissionRequest {
    @NotBlank
    @Schema(description = "文件唯一Id列表")
    private List<String> fileIdList;

    public List<String> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<String> fileIdList) {
        this.fileIdList = fileIdList;
    }
}
