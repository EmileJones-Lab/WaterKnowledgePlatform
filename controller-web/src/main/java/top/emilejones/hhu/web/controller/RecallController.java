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
import top.emilejones.hhu.service.IRecallService;
import top.emilejones.hhu.web.vo.TextNodeVO;

import java.util.List;


@RestController
@RequestMapping("/recall")
@Tag(name = "retrieval", description = "关于召回文本的功能接口说明")
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
                                    description = "文本片段"
                            ),
                            arraySchema = @Schema(
                                    description = "文本片段列表"
                            )
                    )
            )
    )
    public List<String> recallText(@RequestParam("query") @Schema(description = "用户的问题") String query) {
        return recallService.recallText(query);
    }

    @GetMapping("/nodeList")
    @Operation(
            summary = "通过问题去召回相关片段，并且返回每一个片段的详细信息",
            description = "根据问题去图数据库中召回相关片段，并且基于已有策略去图数据库中查询上下文，最后返回每一个片段的详细信息"
    )
    @ApiResponse(responseCode = "200", description = "成功召回文本")
    public List<TextNodeVO> recallNode(@RequestParam("query") @Schema(description = "用户的问题") String query) {
        return recallService.recallNode(query).stream()
                .map(Pair::getSecond)
                .map(neo4jTextNode -> {
                    TextNodeVO textNodeVO = new TextNodeVO();
                    textNodeVO.setText(neo4jTextNode.getText());
                    textNodeVO.setLevel(neo4jTextNode.getLevel());
                    textNodeVO.setSeq(neo4jTextNode.getSeq());
                    textNodeVO.setElementId(neo4jTextNode.getElementId());
                    textNodeVO.setType(neo4jTextNode.getType());
                    return textNodeVO;
                }).toList();
    }
}
