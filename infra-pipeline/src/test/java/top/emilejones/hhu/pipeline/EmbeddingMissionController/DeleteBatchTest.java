package top.emilejones.hhu.pipeline.EmbeddingMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.EmbeddingMissionController;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.Collections;
import java.util.List;

/**
 * 测试 EmbeddingMissionController 的 deleteBatch 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteBatchTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证批量删除任务的功能是否按预期抛出不支持异常。
     */
    @Test
    public void deleteBatch() {
        List<String> ids = Collections.singletonList("id");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            embeddingMissionController.deleteBatch(ids);
        });
    }
}
