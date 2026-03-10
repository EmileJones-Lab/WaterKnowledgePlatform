package top.emilejones.hhu.pipeline.ProcessedDocumentController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.ProcessedDocumentController;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

/**
 * 测试 ProcessedDocumentController 的 save(ProcessedDocument) 方法（不支持的操作）。
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveUnsupportedTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证不支持的 save 方法是否按预期抛出异常。
     */
    @Test
    public void saveUnsupported() {
        ProcessedDocument doc = Mockito.mock(ProcessedDocument.class);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            processedDocumentController.save(doc);
        });
    }
}
