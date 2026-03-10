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
 * 测试 EmbeddingMissionController 的 findBySourceDocumentId 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {
    @Mock
    private EmbeddingMissionService embeddingMissionService;
    @InjectMocks
    private EmbeddingMissionController embeddingMissionController;

    /**
     * 验证根据源文档 ID 查询任务的功能是否正确分发到 service 层。
     */
    @Test
    public void findBySourceDocumentId() {
        String docId = "docId";
        List<EmbeddingMission> missions = Collections.singletonList(Mockito.mock(EmbeddingMission.class));
        Mockito.when(embeddingMissionService.findBySourceDocumentId(docId)).thenReturn(missions);

        List<EmbeddingMission> result = embeddingMissionController.findBySourceDocumentId(docId);
        Assertions.assertEquals(missions, result);
    }
}
