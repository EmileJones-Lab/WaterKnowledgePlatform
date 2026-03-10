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

/**
 * 测试 StructureExtractionMissionController 的 find 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class FindTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证根据 ID 查询任务的功能。
     */
    @Test
    public void find() {
        String id = "testId";
        StructureExtractionMission expectedMission = Mockito.mock(StructureExtractionMission.class);
        Mockito.when(structureExtractionMissionService.findById(id)).thenReturn(expectedMission);

        StructureExtractionMission result = structureExtractionMissionController.find(id);
        Assertions.assertEquals(expectedMission, result);
    }
}
