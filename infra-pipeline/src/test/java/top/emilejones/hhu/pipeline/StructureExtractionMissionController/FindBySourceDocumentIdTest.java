package top.emilejones.hhu.pipeline.StructureExtractionMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.StructureExtractionMissionController;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.Collections;
import java.util.List;

/**
 * 测试 StructureExtractionMissionController 的 findBySourceDocumentId 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证根据源文档 ID 查询结构化抽取任务的功能。
     */
    @Test
    public void findBySourceDocumentId() {
        String docId = "docId";
        List<StructureExtractionMission> expectedMissions = Collections.singletonList(Mockito.mock(StructureExtractionMission.class));

        Mockito.when(structureExtractionMissionService.findBySourceDocumentId(docId)).thenReturn(expectedMissions);

        List<StructureExtractionMission> result = structureExtractionMissionController.findBySourceDocumentId(docId);
        Assertions.assertEquals(expectedMissions, result);
    }
}
