package top.emilejones.hhu.pipeline.EmbeddingMissionController;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.EmbeddingMissionController;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.Collections;
import java.util.List;

/**
 * 测试 EmbeddingMissionController 的 saveBatch 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveBatchTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证批量保存任务的功能是否正确调用了 service 层。
     */
    @Test
    public void saveBatch() {
        List<EmbeddingMission> missions = Collections.singletonList(Mockito.mock(EmbeddingMission.class));
        embeddingMissionController.saveBatch(missions);
        Mockito.verify(embeddingMissionService).saveBatch(missions);
    }
}
