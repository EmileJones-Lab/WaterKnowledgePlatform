package top.emilejones.hhu.web.vo.mission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "请求开启一个文件层次结构提取任务的请求体")
public class StartExtractStructureMissionRequest {
    @NotEmpty
    @Schema(description = "文件唯一Id列表")
    private List<@NotBlank String> fileIdList;

    public List<String> getFileIdList() {
        return fileIdList;
    }

    public void setFileIdList(List<String> fileIdList) {
        this.fileIdList = fileIdList;
    }
}
