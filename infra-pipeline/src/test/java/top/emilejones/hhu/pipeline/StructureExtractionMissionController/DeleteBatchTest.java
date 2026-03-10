package top.emilejones.hhu.pipeline.StructureExtractionMissionController;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.controller.StructureExtractionMissionController;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.Collections;
import java.util.List;

/**
 * 测试 StructureExtractionMissionController 的 deleteBatch 方法。
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteBatchTest {
    @Mock
    private StructureExtractionMissionService structureExtractionMissionService;
    @InjectMocks
    private StructureExtractionMissionController structureExtractionMissionController;

    /**
     * 验证批量删除功能是否抛出不支持异常。
     */
    @Test
    public void deleteBatch() {
        List<String> ids = Collections.singletonList("id");
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            structureExtractionMissionController.deleteBatch(ids);
        });
    }
}
