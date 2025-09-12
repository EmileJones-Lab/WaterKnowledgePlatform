package top.emilejones.hhu.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.emilejones.hhu.mcp.service.IRecallService;

import java.util.List;

@RestController
@RequestMapping("recall")
public class RecallController {

    private final IRecallService recallService;

    public RecallController(IRecallService recallService) {
        this.recallService = recallService;
    }

    @GetMapping("/textList")
    public List<String> recallText(@RequestParam("query") String query) {
        List<String> textList = recallService.recallText(query);
        return textList;
    }
}
