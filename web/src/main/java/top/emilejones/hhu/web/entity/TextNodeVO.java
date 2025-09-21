package top.emilejones.hhu.web.entity;

/**
 * 返回给客户端的数据格式
 *
 * @author EmileJones
 */
public class TextNodeVO {
    private String elementId;
    private String text;
    private Integer seq;

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

    @Override
    public String toString() {
        return "TextNodeVO{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                ", seq=" + seq +
                '}';
    }

    public static TextNodeVO from(TextNode node) {
        TextNodeVO textNodeVO = new TextNodeVO();
        textNodeVO.setElementId(node.getElementId());
        textNodeVO.setText(node.getText());
        textNodeVO.setSeq(node.getSeq());
        return textNodeVO;
    }
}
