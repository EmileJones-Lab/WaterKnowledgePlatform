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
 * 测试 OcrMissionController 的 findBatchBySourceDocumentId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBatchBySourceDocumentIdTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证批量根据源文档 ID 查询 OCR 任务的功能。
     */
    @Test
    public void findBatchBySourceDocumentId() {
        List<String> ids = Collections.singletonList("docId");
        List<List<OcrMission>> expectedMissions = Collections.singletonList(Collections.singletonList(Mockito.mock(OcrMission.class)));

        Mockito.when(ocrMissionService.findBatchBySourceDocumentId(ids)).thenReturn(expectedMissions);

        List<List<OcrMission>> result = ocrMissionController.findBatchBySourceDocumentId(ids);
        Assertions.assertEquals(expectedMissions, result);
    }
}
