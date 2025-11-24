package top.emilejones.hhu.web.vo.mission.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "请求开启一个文件层次结构提取任务的请求体")
public class StartExtractStructureMissionRequest {
    @NotBlank
    @Schema(description = "文件唯一Id")
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
