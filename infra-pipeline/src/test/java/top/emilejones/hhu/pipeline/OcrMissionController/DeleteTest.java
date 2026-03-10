package top.emilejones.hhu.pipeline.OcrMissionController;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.OcrMissionController;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

/**
 * 测试 OcrMissionController 的 delete 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证 delete 方法是否正确分发。
     */
    @Test
    public void delete() {
        String id = "testId";
        ocrMissionController.delete(id);
        Mockito.verify(ocrMissionService).delete(id);
    }
}
