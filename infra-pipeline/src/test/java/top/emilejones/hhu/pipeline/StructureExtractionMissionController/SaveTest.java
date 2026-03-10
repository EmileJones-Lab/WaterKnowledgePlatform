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

/**
 * 测试 StructureExtractionMissionController 的 save 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证 save 方法是否正确调用了 service 层的 save 操作。
     */
    @Test
    public void save() {
        StructureExtractionMission mission = Mockito.mock(StructureExtractionMission.class);
        structureExtractionMissionController.save(mission);
        Mockito.verify(structureExtractionMissionService).save(mission);
    }
}
