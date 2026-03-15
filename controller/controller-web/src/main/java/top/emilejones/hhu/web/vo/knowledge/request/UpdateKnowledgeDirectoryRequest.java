package top.emilejones.hhu.web.vo.knowledge.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "修改一个知识库的请求")
public class UpdateKnowledgeDirectoryRequest {
    @NotBlank
    @Schema(description = "文件夹名称")
    private String dirName;

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }
}
