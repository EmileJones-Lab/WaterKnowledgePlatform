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

import java.util.Collections;
import java.util.List;

/**
 * 测试 OcrMissionController 的 findBySourceDocumentId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证根据源文档 ID 查询 OCR 任务的功能。
     */
    @Test
    public void findBySourceDocumentId() {
        String docId = "docId";
        List<OcrMission> expectedMissions = Collections.singletonList(Mockito.mock(OcrMission.class));

        Mockito.when(ocrMissionService.findBySourceDocumentId(docId)).thenReturn(expectedMissions);

        List<OcrMission> result = ocrMissionController.findBySourceDocumentId(docId);
        Assertions.assertEquals(expectedMissions, result);
    }
}
