package top.emilejones.hhu.pipeline.ProcessedDocumentController;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.ProcessedDocumentController;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.InputStream;

/**
 * 测试 ProcessedDocumentController 的 save(ProcessedDocument, InputStream) 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveWithContentTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证带内容的保存方法是否正确分发到 service 层。
     */
    @Test
    public void saveWithContent() {
        ProcessedDocument doc = Mockito.mock(ProcessedDocument.class);
        InputStream inputStream = Mockito.mock(InputStream.class);

        processedDocumentController.save(doc, inputStream);
        Mockito.verify(processedDocumentService).save(doc, inputStream);
    }
}
