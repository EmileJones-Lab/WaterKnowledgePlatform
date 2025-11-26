package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.web.vo.FailureVO;
import top.emilejones.hhu.web.vo.LazyPageInfoVO;
import top.emilejones.hhu.web.vo.mission.DocumentSplittingMissionVO;
import top.emilejones.hhu.web.vo.mission.EmbeddingMissionVO;
import top.emilejones.hhu.web.vo.mission.MissionsVO;
import top.emilejones.hhu.web.vo.mission.request.StartEmbeddingMissionRequest;
import top.emilejones.hhu.web.vo.mission.request.StartExtractStructureMissionRequest;

@RestController
@RequestMapping("/missions")
@Tag(name = "Mission", description = "关于任务的接口说明")
public class MissionController {

    @PostMapping("/extract-structure-missions")
    @Operation(summary = "开启一个结构提取任务",
            description = "通过文件唯一Id开启一个文本结构提取任务。如果之前没有开启过OCR任务，则自动开启一个OCR任务。")
    @ApiResponse(responseCode = "200", description = "返回此结构提取任务的详细信息")
    public DocumentSplittingMissionVO startExtractStructureMission(@RequestBody StartExtractStructureMissionRequest request) {
        return null;
    }

    @DeleteMapping("/extract-structure-missions/{documentSplittingMissionId}")
    @Operation(summary = "删除结构提取任务",
            description = "通过结构提取任务的唯一Id删除结构提取任务，包括其生成的结构树，以及向量化后的知识文件。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功，什么都不返回"),
            @ApiResponse(
                    responseCode = "500",
                    description = "删除失败",
                    content = @Content(
                            schema = @Schema(implementation = FailureVO.class)
                    )
            )
    })
    public void deleteExtractStructureMission(
            @PathVariable("documentSplittingMissionId") @Schema(name = "documentSplittingMissionId", description = "文本切割任务唯一Id") String documentSplittingMissionId
    ) {
    }


    @PostMapping("/embedding-missions")
    @Operation(summary = "开启一个层次结构向量化任务",
            description = "通过文件唯一Id开启一个向量化任务。如果此文件没有开启过OCR任务和结构提取任务，此接口会自动按顺序开启上述任务。")
    @ApiResponse(responseCode = "200", description = "返回当前向量化任务的详细信息")
    public EmbeddingMissionVO startEmbeddingMission(@RequestBody StartEmbeddingMissionRequest request) {
        return null;
    }
}
