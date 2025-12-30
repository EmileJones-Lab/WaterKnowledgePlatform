package top.emilejones.hhu.application.dto.retrieval;

public class TextNodeDTO {
    private String id;
    private String text;
    private Integer seq;
    private Integer level;
    private TextType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public TextType getType() {
        return type;
    }

    public void setType(TextType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TextNodeDTO{" +
                "id='" + id + "'" +
                ", text='" + text + "'" +
                ", seq=" + seq +
                ", level=" + level +
                ", type=" + type +
                "}";
    }
}
