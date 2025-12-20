package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.application.KnowledgeApplicationService;
import top.emilejones.hhu.application.dto.LazyPageDTO;
import top.emilejones.hhu.application.dto.knowledge.CandidateKnowledgeFileDTO;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeFileDTO;
import top.emilejones.hhu.web.utils.VoConverter;
import top.emilejones.hhu.web.vo.FailureVO;
import top.emilejones.hhu.web.vo.LazyPageInfoVO;
import top.emilejones.hhu.web.vo.knowledge.CandidateKnowledgeFileVO;
import top.emilejones.hhu.web.vo.knowledge.KnowledgeFileVO;
import top.emilejones.hhu.web.vo.knowledge.request.AddKnowledgeFileRequest;

import java.util.Collections;

@RestController
@RequestMapping("/knowledge-repositories/{dirId}")
@Tag(name = "KnowledgeFiles", description = "关于知识文件操作的接口说明")
public class KnowledgeFileController {

    private final KnowledgeApplicationService knowledgeApplicationService;

    public KnowledgeFileController(KnowledgeApplicationService knowledgeApplicationService) {
        this.knowledgeApplicationService = knowledgeApplicationService;
    }

    @GetMapping("/files")
    @Operation(summary = "获取知识库中的知识文件详细信息列表",
            description = "根据知识库唯一Id获取此文件夹下的文件详细信息列表，如果没有数据，则返回的数据中的data为空列表")
    public LazyPageInfoVO<KnowledgeFileVO> getAllKnowledgeFileByDirId(
            @PathVariable("dirId") @Schema(name = "dirId", description = "知识库唯一Id") String dirId,
            @RequestParam("limit") @Schema(name = "limit", description = "每页多少个数据") Integer limit,
            @RequestParam("pageNum") @Schema(name = "pageNum", description = "第几页（从0开始）") Integer pageNum,
            @RequestParam(value = "keyword", required = false) @Schema(name = "keyword", description = "根据文件名模糊匹配，如果为空则返回全部内容。") String keyword
    ) {
        LazyPageDTO<KnowledgeFileDTO> page = knowledgeApplicationService.getAllKnowledgeFileByDirId(dirId, limit, pageNum, keyword);
        return new LazyPageInfoVO<>(page.hasNextPage(), VoConverter.toKnowledgeFileVOList(page.data()));
    }

    @PostMapping("/files")
    @Operation(summary = "向知识库中添加一个知识文件",
            description = "向知识库中添加一个知识文件，只可以添加通过接口/knowledge-repositories/{dirId}/candidate-files获取到的文件列表中的文件。")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "成功将知识文件加入到知识库中后，返回此知识文件的元信息。"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "添加失败，操作不合法，此文件无法加入到此文件夹中",
                    content = @Content(
                            schema = @Schema(implementation = FailureVO.class)
                    )
            )
    })
    public KnowledgeFileVO addKnowledgeFileByDirId(
            @PathVariable("dirId") @Schema(name = "dirId", description = "知识库唯一Id") String dirId,
            @RequestBody AddKnowledgeFileRequest request
    ) {
        KnowledgeFileDTO knowledgeFileDTO = knowledgeApplicationService.addKnowledgeFileByDirId(dirId, request.getKnowledgeFileId());
        return VoConverter.toKnowledgeFileVO(knowledgeFileDTO);
    }

    @DeleteMapping("/files/{knowledgeFileId}")
    @Operation(summary = "删除知识库中的一个知识文件",
            description = "将知识文件从此知识库中删除")
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
    public void deleteKnowledgeFileByDirId(
            @PathVariable("dirId") @Schema(name = "dirId", description = "知识库唯一Id") String dirId,
            @PathVariable("knowledgeFileId") @Schema(name = "knowledgeFileId", description = "知识文件唯一Id") String knowledgeFileId
    ) {
        knowledgeApplicationService.deleteKnowledgeFileByDirId(dirId, Collections.singletonList(knowledgeFileId));
    }

    @GetMapping("/candidate-files")
    @Operation(summary = "获取可以加入到这个知识库中的文件列表",
            description = "这个接口会分页的返回可以加入到这个知识库中的所有文件文件信息")
    public LazyPageInfoVO<CandidateKnowledgeFileVO> getAllCandidateFiles(
            @PathVariable("dirId") @Schema(name = "dirId", description = "知识库唯一Id") String dirId,
            @RequestParam("limit") @Schema(name = "limit", description = "每页多少条数据") Integer limit,
            @RequestParam("pageNum") @Schema(name = "pageNum", description = "第几页数据（下标从0开始）") Integer pageNum,
            @RequestParam(name = "keyWord", required = false) @Schema(name = "keyWord", description = "需要查询的关键字") String keyWord
    ) {
        LazyPageDTO<CandidateKnowledgeFileDTO> page = knowledgeApplicationService.getAllCandidateFiles(dirId, limit, pageNum, keyWord);
        return new LazyPageInfoVO<>(page.hasNextPage(), VoConverter.toCandidateKnowledgeFileVOList(page.data()));
    }
}
