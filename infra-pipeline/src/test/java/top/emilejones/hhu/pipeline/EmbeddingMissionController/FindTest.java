package top.emilejones.hhu.pipeline.EmbeddingMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.EmbeddingMissionController;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

/**
 * 测试 EmbeddingMissionController 的 find 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证 find 方法是否正确调用了 service 层的 findById 操作并返回预期结果。
     */
    @Test
    public void find() {
        String id = "testId";
        EmbeddingMission mission = Mockito.mock(EmbeddingMission.class);
        Mockito.when(embeddingMissionService.findById(id)).thenReturn(mission);

        EmbeddingMission result = embeddingMissionController.find(id);
        Assertions.assertEquals(mission, result);
    }
}
