package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.web.vo.*;
import top.emilejones.hhu.web.vo.mission.*;
import top.emilejones.hhu.web.vo.mission.request.StartEmbeddingMissionRequest;
import top.emilejones.hhu.web.vo.mission.request.StartExtractStructureMissionRequest;

@RestController
@RequestMapping("/missions")
@Tag(name = "Mission", description = "关于任务的接口说明")
public class MissionController {

    @GetMapping
    @Operation(summary = "分页的获取已经开启了任意任务的文件信息列表",
            description = "按照文件创建时间倒序查询任务列表，该接口会返回当页的文件详细信息，如果当前页面没有内容，那么会返回空列表。没有开启任何任务的文件，不会被返回。")
    @ApiResponse(responseCode = "200", description = "成功查询到数据")
    public LazyPageInfoVO<MissionsVO> getMissionsList(
            @RequestParam @Schema(name = "limit", description = "每页多少个数据") Integer limit,
            @RequestParam @Schema(name = "pageNum", description = "第几页（从0开始）") Integer pageNum
    ) {
        return null;
    }

    @PostMapping("/extract-structure-missions")
    @Operation(summary = "开启一个结构提取任务",
            description = "通过文件唯一Id开启一个文本结构提取任务。如果之前没有开启过OCR任务，则自动开启一个OCR任务。")
    @ApiResponse(responseCode = "200", description = "返回此结构提取任务的详细信息")
    public ExtractStructureMissionVO startExtractStructureMission(@RequestBody StartExtractStructureMissionRequest request) {
        return null;
    }


    @PostMapping("/embedding-missions")
    @Operation(summary = "开启一个层次结构向量化任务",
            description = "通过文件唯一Id开启一个OCR任务。如果此文件没有开启过OCR任务和结构提取任务，此接口会自动按顺序开启上述任务。")
    @ApiResponse(responseCode = "200", description = "返回当前向量化任务的详细信息")
    public EmbeddingMissionVO startEmbeddingMission(@RequestBody StartEmbeddingMissionRequest request) {
        return null;
    }
}
