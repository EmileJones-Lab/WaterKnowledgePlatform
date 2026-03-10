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

import java.util.Collections;
import java.util.List;

/**
 * 测试 EmbeddingMissionController 的 findBatchBySourceDocumentId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBatchBySourceDocumentIdTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证批量根据源文档 ID 查询任务的功能。
     */
    @Test
    public void findBatchBySourceDocumentId() {
        List<String> ids = Collections.singletonList("docId");
        List<List<EmbeddingMission>> missions = Collections.singletonList(Collections.singletonList(Mockito.mock(EmbeddingMission.class)));
        Mockito.when(embeddingMissionService.findBatchBySourceDocumentId(ids)).thenReturn(missions);

        List<List<EmbeddingMission>> result = embeddingMissionController.findBatchBySourceDocumentId(ids);
        Assertions.assertEquals(missions, result);
    }
}
