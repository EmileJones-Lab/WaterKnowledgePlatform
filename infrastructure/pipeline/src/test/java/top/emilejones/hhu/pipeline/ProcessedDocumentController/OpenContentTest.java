package top.emilejones.hhu.pipeline.ProcessedDocumentController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.ProcessedDocumentController;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.InputStream;

/**
 * 测试 ProcessedDocumentController 的 openContent 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class OpenContentTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证根据路径打开文件内容的功能。
     */
    @Test
    public void openContent() {
        String filePath = "/path/to/doc.md";
        InputStream expectedStream = Mockito.mock(InputStream.class);
        Mockito.when(processedDocumentService.openContent(filePath)).thenReturn(expectedStream);

        InputStream result = processedDocumentController.openContent(filePath);
        Assertions.assertEquals(expectedStream, result);
    }
}
