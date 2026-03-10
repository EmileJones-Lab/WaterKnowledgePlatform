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

import java.util.Optional;

/**
 * 测试 ProcessedDocumentController 的 find 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private ProcessedDocumentService processedDocumentService;
    @InjectMocks
    private ProcessedDocumentController processedDocumentController;

    /**
     * 验证根据 ID 查询文档的功能。
     */
    @Test
    public void find() {
        String id = "testId";
        ProcessedDocument doc = Mockito.mock(ProcessedDocument.class);
        Mockito.when(processedDocumentService.findById(id)).thenReturn(Optional.of(doc));

        ProcessedDocument result = processedDocumentController.find(id);
        Assertions.assertEquals(doc, result);
    }

    /**
     * 验证查询不存在的文档时返回 null。
     */
    @Test
    public void findNotFound() {
        String id = "nonExist";
        Mockito.when(processedDocumentService.findById(id)).thenReturn(Optional.empty());

        ProcessedDocument result = processedDocumentController.find(id);
        Assertions.assertNull(result);
    }
}
