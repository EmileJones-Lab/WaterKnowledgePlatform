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

import java.util.Collections;
import java.util.List;

/**
 * 测试 OcrMissionController 的 saveBatch 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveBatchTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证批量保存 OCR 任务功能是否正确调用。
     */
    @Test
    public void saveBatch() {
        List<OcrMission> missions = Collections.singletonList(Mockito.mock(OcrMission.class));
        ocrMissionController.saveBatch(missions);
        Mockito.verify(ocrMissionService).saveBatch(missions);
    }
}
