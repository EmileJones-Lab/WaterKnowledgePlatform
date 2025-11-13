package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "retrieve", description = "关于召回文本的功能接口说明")
public class RecallController {

    private final IRecallService recallService;

    public RecallController(IRecallService recallService) {
        this.recallService = recallService;
    }

    @GetMapping("/textList")
    @Operation(
            summary = "通过问题去召回相关片段",
            description = "根据问题去图数据库中召回相关片段，并且基于已有策略去图数据库中查询上下文，最后返回一个片段的列表"
    )
    @ApiResponse(responseCode = "200", description = "成功召回文本",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = String.class,
                                    type = "array"
                            ))
            )
    )
    public List<String> recallText(@RequestParam("query") String query) {
        return recallService.recallText(query);
    }

    @GetMapping("/nodeList")
    @Operation(
            summary = "通过问题去召回相关片段，并且返回每一个片段的详细信息",
            description = "根据问题去图数据库中召回相关片段，并且基于已有策略去图数据库中查询上下文，最后返回每一个片段的详细信息"
    )
    @ApiResponse(responseCode = "200", description = "成功召回文本",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = Neo4jTextNode.class,
                                    type = "array"
                            )
                    )
            )
    )
    public List<Neo4jTextNode> recallNode(@RequestParam("query") String query) {
        return recallService.recallNode(query).stream().map(Pair::getSecond).toList();
    }
}
