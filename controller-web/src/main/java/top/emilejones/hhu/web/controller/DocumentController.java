package top.emilejones.hhu.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.emilejones.hhu.web.vo.FailureVO;
import top.emilejones.hhu.web.vo.retrieval.TextNodeVO;

import java.util.List;

@RestController
@RequestMapping("/source-documents")
@Tag(name = "DocumentManager", description = "关于文件管理的相关接口")
public class DocumentController {

    @GetMapping("/{fileId}/structure")
    @Operation(summary = "获取一个文件的层次结构",
            description = "如果该文件进行了结构提取任务，并且任务成功，则返回结构数据。否则，返回HTTP状态码409")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "文件结构化数据"),
            @ApiResponse(
                    responseCode = "409",
                    description = "当文件结构没有被提取时，返回错误信息",
                    content = @Content(
                            schema = @Schema(
                                    implementation = FailureVO.class
                            )
                    )
            )
    })
    public List<TextNodeVO> getFileStructureByFileId(
            @PathVariable("fileId") @Schema(description = "文件的唯一Id") String fileId
    ) {
        return null;
    }
}
