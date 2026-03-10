package top.emilejones.hhu.pipeline.ProcessedDocumentController;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.ProcessedDocumentController;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

/**
 * 测试 ProcessedDocumentController 的 deleteBySourceDocumentId 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteBySourceDocumentIdTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证根据源文档 ID 删除的功能。
     */
    @Test
    public void deleteBySourceDocumentId() {
        String sourceId = "sourceId";
        processedDocumentController.deleteBySourceDocumentId(sourceId);
        Mockito.verify(processedDocumentService).deleteBySourceDocumentId(sourceId);
    }
}
