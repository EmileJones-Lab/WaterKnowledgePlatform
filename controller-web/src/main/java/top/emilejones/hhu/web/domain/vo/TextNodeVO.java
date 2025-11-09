package top.emilejones.hhu.web.domain.vo;

import kotlin.Pair;
import top.emilejones.hhu.entity.FileNode;
import top.emilejones.hhu.entity.TextNode;

/**
 * 返回给客户端的数据格式
 *
 * @author EmileJones
 */
public class TextNodeVO {
    private String elementId;
    private String text;
    private Integer seq;
    private String filename;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "TextNodeVO{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                ", seq=" + seq +
                ", filename='" + filename + '\'' +
                '}';
    }

    public static TextNodeVO from(Pair<FileNode, TextNode> datum) {
        TextNodeVO textNodeVO = new TextNodeVO();
        textNodeVO.setElementId(datum.getSecond().getElementId());
        textNodeVO.setText(datum.getSecond().getText());
        textNodeVO.setSeq(datum.getSecond().getSeq());
        textNodeVO.setFilename(datum.getFirst().getFileName());
        return textNodeVO;
    }
}
