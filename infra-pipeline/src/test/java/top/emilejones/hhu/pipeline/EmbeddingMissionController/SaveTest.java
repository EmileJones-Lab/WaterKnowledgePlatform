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

/**
 * 测试 EmbeddingMissionController 的 save 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证 save 方法是否正确调用了 service 层的 save 操作。
     */
    @Test
    public void save() {
        EmbeddingMission mission = Mockito.mock(EmbeddingMission.class);
        embeddingMissionController.save(mission);
        Mockito.verify(embeddingMissionService).save(mission);
    }
}
