package top.emilejones.hhu.web.vo.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "知识库元数据")
public class KnowledgeDirectoryVO {
    @Schema(description = "知识库唯一Id")
    private String id;
    @Schema(description = "对应的向量数据库的Collection名称")
    private String colName;
    @Schema(description = "知识库名称")
    private String kbName;
    @Schema(description = "创建时间")
    private Instant createTime;
    @Schema(description = "知识库类型")
    private KnowledgeDirectoryType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getKbName() {
        return kbName;
    }

    public void setKbName(String kbName) {
        this.kbName = kbName;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public KnowledgeDirectoryType getType() {
        return type;
    }

    public void setType(KnowledgeDirectoryType type) {
        this.type = type;
    }
}
