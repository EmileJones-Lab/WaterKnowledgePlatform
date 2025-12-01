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
import top.emilejones.hhu.web.vo.mission.MissionsVO;
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

    @GetMapping
    @Operation(summary = "分页的获取已经开启了任意任务的文件信息列表",
            description = "按照文件创建时间倒序查询任务列表，该接口会返回当页的文件详细信息，如果当前页面没有内容，那么会返回空列表。如果hasMission参数的值为true，则不会被返回没有开启任何任务的文件。目前此方法只支持hasMission参数的值为true的请求。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功查询到数据"),
            @ApiResponse(
                    responseCode = "501",
                    description = "暂未实现此接口",
                    content = @Content(
                            schema = @Schema(
                                    implementation = FailureVO.class
                            )
                    )
            )
    })
    public LazyPageInfoVO<MissionsVO> getMissionsList(
            @RequestParam("limit") @Schema(name = "limit", description = "每页多少个数据") Integer limit,
            @RequestParam("pageNum") @Schema(name = "pageNum", description = "第几页（从0开始）") Integer pageNum,
            @RequestParam(required = false) @Schema(name = "keyword", description = "模糊匹配文件名，如果为空则返回全部数据") String keyword,
            @RequestParam("hasMission") @Schema(name = "hasMission", description = "是否只返回有任务开启的文件列表？（目前只支持true）") Boolean hasMission
    ) {
        return null;
    }
}
