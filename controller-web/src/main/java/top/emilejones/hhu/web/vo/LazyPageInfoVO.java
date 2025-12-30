package top.emilejones.hhu.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分页数据")
public record LazyPageInfoVO<T>(
        @Schema(description = "是否有下一页") Boolean hasNextPage,
        @Schema(description = "本页数据") List<T> data
) {

}
