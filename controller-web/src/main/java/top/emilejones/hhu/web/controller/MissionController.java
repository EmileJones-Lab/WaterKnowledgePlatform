package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.application.PipeLineApplicationService;
import top.emilejones.hhu.application.dto.mission.DocumentSplittingMissionDTO;
import top.emilejones.hhu.application.dto.mission.EmbeddingMissionDTO;
import top.emilejones.hhu.web.utils.VoConverter;
import top.emilejones.hhu.web.vo.FailureVO;
import top.emilejones.hhu.web.vo.mission.DocumentSplittingMissionVO;
import top.emilejones.hhu.web.vo.mission.EmbeddingMissionVO;
import top.emilejones.hhu.web.vo.mission.request.StartEmbeddingMissionRequest;
import top.emilejones.hhu.web.vo.mission.request.StartExtractStructureMissionRequest;

import java.util.List;

@RestController
@RequestMapping("/missions")
@Tag(name = "Mission", description = "关于任务的接口说明")
public class MissionController {

    private final PipeLineApplicationService pipeLineApplicationService;

    public MissionController(PipeLineApplicationService pipeLineApplicationService) {
        this.pipeLineApplicationService = pipeLineApplicationService;
    }

    @PostMapping("/extract-structure-missions")
    @Operation(summary = "批量开启结构提取任务",
            description = "通过文件唯一Id开启一个文本结构提取任务。如果之前没有开启过OCR任务，则自动开启一个OCR任务。")
    @ApiResponse(responseCode = "200", description = "返回这批结构提取任务的详细信息")
    public List<DocumentSplittingMissionVO> startExtractStructureMission(@RequestBody StartExtractStructureMissionRequest request) {
        List<DocumentSplittingMissionDTO> missionDTOS = pipeLineApplicationService.startStructureExtractionMission(request.getFileIdList());
        return VoConverter.toDocumentSplittingMissionVOList(missionDTOS);
    }

    @DeleteMapping("/extract-structure-missions/{fileId}")
    @Operation(summary = "删除结构提取任务",
            description = "通过文件Id删除结构提取任务，包括其生成的结构树，以及向量化后的知识文件。")
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
            @PathVariable("fileId") @Schema(name = "fileId", description = "文件唯一Id") String fileId
    ) {
        pipeLineApplicationService.deleteExtractStructureMission(fileId);
    }


    @PostMapping("/embedding-missions")
    @Operation(summary = "批量开启层次结构向量化任务",
            description = "通过文件唯一Id批量开启向量化任务。如果此文件没有开启过OCR任务和结构提取任务，此接口会自动按顺序开启上述任务。")
    @ApiResponse(responseCode = "200", description = "返回这批向量化任务的详细信息")
    public List<EmbeddingMissionVO> startEmbeddingMission(@RequestBody StartEmbeddingMissionRequest request) {
        List<EmbeddingMissionDTO> missionDTOS = pipeLineApplicationService.startEmbeddingMission(request.getFileIdList());
        return VoConverter.toEmbeddingMissionVOList(missionDTOS);
    }
}
