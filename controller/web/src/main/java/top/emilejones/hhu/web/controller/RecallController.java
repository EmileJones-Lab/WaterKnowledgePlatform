package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.application.platform.RecallApplicationService;

import java.util.List;


@RestController
@RequestMapping("/knowledge-repositories/{catalogId}")
@Tag(name = "Retrieval", description = "关于召回文本的功能接口说明")
public class RecallController {

    private final RecallApplicationService recallService;

    public RecallController(RecallApplicationService recallService) {
        this.recallService = recallService;
    }

    @GetMapping("/texts")
    @Operation(
            summary = "通过问题去相关的知识库中召回相关片段",
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
    public List<String> recallText(
            @RequestParam("query") @Schema(description = "用户的问题") String query,
            @PathVariable("catalogId") @Schema(description = "知识库名称") String knowledgeDocumentId) {
        return recallService.recallText(query, knowledgeDocumentId);
    }

}
