package top.emilejones.hhu.mcp;


public class TextNode {

    private String elementId;
    private Integer id;
    private Integer level;
    private Integer seq;
    private Integer name;
    private String text;

    // 构造函数
    public TextNode() {}

    public TextNode(String elementId, Integer id, Integer level, Integer seq, Integer name, String text) {
        this.elementId = elementId;
        this.id = id;
        this.level = level;
        this.seq = seq;
        this.name = name;
        this.text = text;
    }

    // getter & setter
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
