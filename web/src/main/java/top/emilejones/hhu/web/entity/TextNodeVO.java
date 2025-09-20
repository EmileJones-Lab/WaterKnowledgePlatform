package top.emilejones.hhu.web.entity;

/**
 * 返回给客户端的数据格式
 *
 * @author EmileJones
 */
public class TextNodeVO {
    private String elementId;
    private String text;

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

    @Override
    public String toString() {
        return "TextNodeVO{" +
                "elementId='" + elementId + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    public static TextNodeVO from(TextNode node) {
        TextNodeVO textNodeVO = new TextNodeVO();
        textNodeVO.setElementId(node.getElementId());
        textNodeVO.setText(node.getText());
        return textNodeVO;
    }
}
