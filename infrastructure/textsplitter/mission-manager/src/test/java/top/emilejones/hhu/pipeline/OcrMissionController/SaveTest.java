package top.emilejones.hhu.pipeline.OcrMissionController;

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
 * 测试 OcrMissionController 的 save 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证 save 方法是否正确调用了 service 层的 save 操作。
     */
    @Test
    public void save() {
        OcrMission mission = Mockito.mock(OcrMission.class);
        ocrMissionController.save(mission);
        Mockito.verify(ocrMissionService).save(mission);
    }
}
