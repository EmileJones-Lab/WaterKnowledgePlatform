package top.emilejones.hhu.mcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McpResponseConverter {

    public static Map<String, Object> fromTextNode(TextNode node) {
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");          // 必须字段
        textContent.put("text", node.getText());  // 必须字段

        // 如果你想把 TextNode 的其他属性也带上，可以附加进去
        textContent.put("elementId", node.getElementId());
        textContent.put("id", node.getId());
        textContent.put("level", node.getLevel());
        textContent.put("seq", node.getSeq());
        textContent.put("name", node.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("content", List.of(textContent));

        return response;
    }
}