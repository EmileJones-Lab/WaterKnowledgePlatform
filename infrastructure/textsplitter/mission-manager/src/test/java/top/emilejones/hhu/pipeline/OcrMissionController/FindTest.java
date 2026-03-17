package top.emilejones.hhu.pipeline.OcrMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.OcrMissionController;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

/**
 * 测试 OcrMissionController 的 find 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证根据 ID 查询任务的功能。
     */
    @Test
    public void find() {
        String id = "testId";
        OcrMission expectedMission = Mockito.mock(OcrMission.class);
        Mockito.when(ocrMissionService.findById(id)).thenReturn(expectedMission);

        OcrMission result = ocrMissionController.find(id);
        Assertions.assertEquals(expectedMission, result);
    }
}
