package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import top.emilejones.hhu.application.KnowledgeApplicationService;
import top.emilejones.hhu.application.dto.knowledge.KnowledgeDirectoryDTO;
import top.emilejones.hhu.web.utils.VoConverter;
import top.emilejones.hhu.web.vo.FailureVO;
import top.emilejones.hhu.web.vo.knowledge.KnowledgeDirectoryVO;
import top.emilejones.hhu.web.vo.knowledge.request.AddKnowledgeDirectoryRequest;
import top.emilejones.hhu.web.vo.knowledge.request.UpdateKnowledgeDirectoryRequest;

import java.util.List;

@RestController
@RequestMapping("/knowledge-repositories")
@Tag(name = "KnowledgeRepositories", description = "关于知识库操作的接口说明")
public class KnowledgeDirectoryController {

    private final KnowledgeApplicationService knowledgeApplicationService;

    public KnowledgeDirectoryController(KnowledgeApplicationService knowledgeApplicationService) {
        this.knowledgeApplicationService = knowledgeApplicationService;
    }

    @GetMapping
    @Operation(summary = "获取知识库列表",
            description = "此接口会返回知识库列表，如果没有数据，则会返回空列表")
    @ApiResponse(responseCode = "200", description = "成功获取知识库列表")
    public List<KnowledgeDirectoryVO> getAllKnowledgeDirectories() {
        List<KnowledgeDirectoryDTO> allKnowledgeDirectories = knowledgeApplicationService.getAllKnowledgeDirectories();
        return VoConverter.toKnowledgeDirectoryVOList(allKnowledgeDirectories);
    }

    @PostMapping
    @Operation(summary = "新增一个知识库",
            description = "此接口会新增一个知识库，并且自动生成一个milvusCollection")
    @ApiResponse(responseCode = "200", description = "新增的知识库元数据")
    public KnowledgeDirectoryVO addKnowledgeDirectory(
            @RequestBody AddKnowledgeDirectoryRequest request
    ) {
        KnowledgeDirectoryDTO knowledgeDirectoryDTO = knowledgeApplicationService.addKnowledgeDirectory(VoConverter.toAddKnowledgeDirectoryDTO(request));
        return VoConverter.toKnowledgeDirectoryVO(knowledgeDirectoryDTO);
    }

    @PutMapping("/{dirId}")
    @Operation(summary = "修改一个知识库元信息",
            description = "此接口目前只支持修改文件夹名称，当修改成功后，会返回修改后的文件夹元信息")
    @ApiResponse(responseCode = "200", description = "成功修改文件夹属性，返回修改后的文件夹元信息")
    public KnowledgeDirectoryVO updateKnowledgeDirectory(
            @RequestBody UpdateKnowledgeDirectoryRequest request,
            @PathVariable("dirId") @Schema(description = "知识库唯一Id") String id
    ) {
        KnowledgeDirectoryDTO knowledgeDirectoryDTO = knowledgeApplicationService.updateKnowledgeDirectory(id, request.getDirName());
        return VoConverter.toKnowledgeDirectoryVO(knowledgeDirectoryDTO);
    }

    @DeleteMapping("/{dirId}")
    @Operation(summary = "删除一个知识库",
            description = "删除一个知识库")
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
    public void deleteKnowledgeDirectory(
            @PathVariable("dirId") @Schema(description = "知识库唯一Id") String id
    ) {
        knowledgeApplicationService.deleteKnowledgeDirectory(id);
    }
}
