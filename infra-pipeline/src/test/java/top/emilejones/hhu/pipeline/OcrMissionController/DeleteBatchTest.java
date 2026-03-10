package top.emilejones.hhu.pipeline.OcrMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.OcrMissionController;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

import java.util.Collections;
import java.util.List;

/**
 * 测试 OcrMissionController 的 deleteBatch 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteBatchTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证批量删除功能是否抛出不支持异常。
     */
    @Test
    public void deleteBatch() {
        List<String> ids = Collections.singletonList("id");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            ocrMissionController.deleteBatch(ids);
        });
    }
}
