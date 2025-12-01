package top.emilejones.hhu.web.vo.knowledge.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "向一个知识库中添加一个知识文件的请求参数")
public class AddKnowledgeFileRequest {
    @NotNull
    @Schema(description = "向量化任务唯一标识")
    private String embeddingMissionId;

    public String getEmbeddingMissionId() {
        return embeddingMissionId;
    }

    public void setEmbeddingMissionId(String embeddingMissionId) {
        this.embeddingMissionId = embeddingMissionId;
    }
}
