package top.emilejones.hhu.web.entity;

public class FileNode {
    private String elementId;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String toString() {
        return "FileNode{" +
                "elementId='" + elementId + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
