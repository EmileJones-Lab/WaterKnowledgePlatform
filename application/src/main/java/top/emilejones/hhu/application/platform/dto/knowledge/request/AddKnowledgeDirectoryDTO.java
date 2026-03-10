package top.emilejones.hhu.application.platform.dto.knowledge.request;

import top.emilejones.hhu.application.platform.dto.knowledge.KnowledgeDirectoryType;

public class AddKnowledgeDirectoryDTO {
    private String dirName;
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
