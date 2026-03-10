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
 * 测试 ProcessedDocumentController 的 delete 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证根据 markdown 文档 ID 删除。
     */
    @Test
    public void delete() {
        String id = "mdId";
        processedDocumentController.delete(id);
        Mockito.verify(processedDocumentService).delete(id);
    }
}
