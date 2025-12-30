package top.emilejones.hhu.application.dto.knowledge;

import java.time.Instant;

public class KnowledgeDirectoryDTO {
    private String id;
    private String colName;
    private String kbName;
    private Instant createTime;
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
