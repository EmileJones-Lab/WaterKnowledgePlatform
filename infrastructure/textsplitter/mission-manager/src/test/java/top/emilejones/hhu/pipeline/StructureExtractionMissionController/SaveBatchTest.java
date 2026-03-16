package top.emilejones.hhu.pipeline.StructureExtractionMissionController;

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
 * 测试 StructureExtractionMissionController 的 saveBatch 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class  SaveBatchTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证批量保存任务的功能。
     */
    @Test
    public void saveBatch() {
        List<StructureExtractionMission> missions = Collections.singletonList(Mockito.mock(StructureExtractionMission.class));
        structureExtractionMissionController.saveBatch(missions);
        Mockito.verify(structureExtractionMissionService).saveBatch(missions);
    }
}
