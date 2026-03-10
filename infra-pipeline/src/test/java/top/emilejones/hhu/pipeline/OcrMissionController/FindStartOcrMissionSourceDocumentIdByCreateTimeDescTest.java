package top.emilejones.hhu.pipeline.OcrMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.OcrMissionController;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

import java.util.Arrays;
import java.util.List;

/**
 * 测试 OcrMissionController 的 findStartOcrMissionSourceDocumentIdByCreateTimeDesc 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindStartOcrMissionSourceDocumentIdByCreateTimeDescTest {
    @Mock
    private OcrMissionService ocrMissionService;
    @InjectMocks
    private OcrMissionController ocrMissionController;

    /**
     * 验证根据创建时间倒序查询开始 OCR 任务的源文档 ID 列表的功能。
     */
    @Test
    public void findStartOcrMissionSourceDocumentIdByCreateTimeDesc() {
        int limit = 10;
        int offset = 0;
        String keyword = "test";
        List<String> expectedIds = Arrays.asList("id1", "id2");

        Mockito.when(ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit, offset, keyword))
                .thenReturn(expectedIds);

        List<String> result = ocrMissionController.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(limit, offset, keyword);
        Assertions.assertEquals(expectedIds, result);
    }
}
