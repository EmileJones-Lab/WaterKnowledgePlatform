package top.emilejones.hhu.web.controller;

import kotlin.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.emilejones.hhu.domain.po.Neo4jTextNode;
import top.emilejones.hhu.service.IRecallService;

import java.util.List;

@RestController
@RequestMapping("/recall")
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
    public List<Neo4jTextNode> recallNode(@RequestParam("query") String query) {
        return recallService.recallNode(query).stream().map(Pair::getSecond).toList();
    }
}
