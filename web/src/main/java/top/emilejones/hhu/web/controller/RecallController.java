package top.emilejones.hhu.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.emilejones.hhu.web.entity.TextNodeVO;
import top.emilejones.hhu.web.service.IRecallService;

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
        return recallService.recallText(query);
    }

    @GetMapping("/nodeList")
    public List<TextNodeVO> recallNode(@RequestParam("query") String query) {
        return recallService.recallNode(query).stream().map(TextNodeVO::from).toList();
    }
}
