package top.emilejones.hhu.pipeline.StructureExtractionMissionController;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.StructureExtractionMissionController;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

/**
 * 测试 StructureExtractionMissionController 的 delete 方法。
 * @author EmileJones
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证 delete 方法是否正确分发。
     */
    @Test
    public void delete() {
        String id = "testId";
        structureExtractionMissionController.delete(id);
        Mockito.verify(structureExtractionMissionService).delete(id);
    }
}
