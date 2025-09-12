package top.emilejones.hhu.mcp.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Controller;
import top.emilejones.hhu.mcp.service.IRecallService;

import java.util.List;
import java.util.Map;

@Controller
public class RecallMcp {
    private IRecallService recallService;

    public RecallMcp(IRecallService recallService) {
        this.recallService = recallService;
    }

    @Tool(description = "根据问题返回和问题最相关的资料片段")
    public Map<String, Object> recallText(@ToolParam(description = "问题") String query) {
        List<String> resourceList = recallService.recallText(query);
        return Map.of("result", resourceList);
    }
}
