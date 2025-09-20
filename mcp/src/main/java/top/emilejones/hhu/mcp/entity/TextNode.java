package top.emilejones.hhu.mcp.entity;


import top.emilejones.hhu.mcp.enums.TextType;

public class TextNode {

    private String elementId;
    private Integer level;
    private Integer seq;
    private Integer name;
    private String text;
    private TextType type;

    // 构造函数
    public TextNode() {}

    public TextNode(String elementId, Integer level, Integer seq, Integer name, String text, TextType textType) {
        this.elementId = elementId;
        this.level = level;
        this.seq = seq;
        this.name = name;
        this.text = text;
        this.type = textType;
    }

    // getter & setter
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Integer getName() {
        return name;
    }

    public void setName(Integer name) {
        this.name = name;
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
        return "TextNode{" +
                "elementId='" + elementId + '\'' +
                ", level=" + level +
                ", seq=" + seq +
                ", name=" + name +
                ", text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
