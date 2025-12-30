package top.emilejones.hhu.web.vo.knowledge.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryType;

@Schema(description = "新增一个知识库的请求")
public class AddKnowledgeDirectoryRequest {
    @NotBlank
    @Schema(description = "文件夹名称")
    private String dirName;

    @NotNull
    @Schema(description = "文件夹类型")
    private KnowledgeDirectoryType type;

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public KnowledgeDirectoryType getType() {
        return type;
    }

    public void setType(KnowledgeDirectoryType type) {
        this.type = type;
    }
}
