package top.emilejones.hhu.application.platform.dto;

import java.util.List;

public record LazyPageDTO<T>(
        Boolean hasNextPage,
        List<T> data
) {
}
