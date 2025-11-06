package top.emilejones.hhu.entity;

import top.emilejones.hhu.enums.TextType;

/**
 * 向量数据库中的一条数据
 *
 * @author EmileJones
 */
public class DenseRecallResult {
    private String elementId;
    private String text;
    private TextType type;
    private Float score;

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

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DenseRecallResult{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                ", type=" + type +
                ", score=" + score +
                '}';
    }
}
