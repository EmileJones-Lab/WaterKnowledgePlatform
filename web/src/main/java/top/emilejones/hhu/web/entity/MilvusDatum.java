package top.emilejones.hhu.web.entity;

import top.emilejones.hhu.web.enums.TextType;

public class MilvusDatum {
    private String elementId;
    private String text;
    private TextType type;

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextType getType() {
        return type;
    }

    public void setType(TextType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MilvusDatum{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
