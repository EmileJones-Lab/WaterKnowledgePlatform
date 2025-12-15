package top.emilejones.hhu.web.vo.knowledge.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "向一个知识库中添加一个知识文件的请求参数")
public class AddKnowledgeFileRequest {
    @NotNull
    @Schema(description = "知识文件唯一Id")
    private String knowledgeFileId;

    public String getKnowledgeFileId() {
        return knowledgeFileId;
    }

    public void setKnowledgeFileId(String knowledgeFileId) {
        this.knowledgeFileId = knowledgeFileId;
    }
}
