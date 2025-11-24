package top.emilejones.hhu.web.vo.retrieval;

import io.swagger.v3.oas.annotations.media.Schema;
import top.emilejones.hhu.enums.TextType;

@Schema(description = "切片节点相关数据")
public class TextNodeVO {
    @Schema(description = "节点唯一ID")
    private String elementId;
    @Schema(description = "切片文本")
    private String text;
    @Schema(description = "切片在当前文件的序号")
    private Integer seq;
    @Schema(description = "此节点属于几级标题（如果是INT_MAX就是文本节点）")
    private Integer level;
    @Schema(description = "这段切片是什么类型的内容")
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

    @Override
    public String toString() {
        return "TextNodeVO{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                ", seq=" + seq +
                ", level=" + level +
                ", type=" + type +
                '}';
    }

    public void setType(TextType type) {
        this.type = type;
    }
}