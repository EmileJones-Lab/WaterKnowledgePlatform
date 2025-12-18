package top.emilejones.hhu.pipeline.StructureExtractionMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.StructureExtractionMissionMapper;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结构化抽取任务删除测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {

    @Autowired
    private StructureExtractionMissionService structureExtractionMissionService;
    @Autowired
    private StructureExtractionMissionMapper structureExtractionMissionMapper;

    private final List<String> createdMissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        structureExtractionMissionMapper.truncateTable();
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        structureExtractionMissionMapper.truncateTable();
    }

    /**
     * 测试删除已存在的结构化抽取任务
     */
    @Test
    public void deleteExistingMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
        assertNotNull(saved);

        structureExtractionMissionService.delete(missionId);
        createdMissionIds.remove(missionId);

        StructureExtractionMission deleted = structureExtractionMissionService.findById(missionId);
        assertNull(deleted, "删除后任务应该不存在");
    }

    /**
     * 测试删除不存在的结构化抽取任务（幂等性）
     */
    @Test
    public void deleteNonExistingMissionTest() {
        String nonExistingId = UUID.randomUUID().toString();
        assertDoesNotThrow(() -> structureExtractionMissionService.delete(nonExistingId));
        StructureExtractionMission mission = structureExtractionMissionService.findById(nonExistingId);
        assertNull(mission);
    }

    /**
     * 测试删除完成的任务
     */
    @Test
    public void deleteCompletedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(UUID.randomUUID().toString());
        mission.success(UUID.randomUUID().toString());

        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
        assertNotNull(saved);
        assertTrue(saved.isCompleted());
        assertTrue(saved.isSuccess());

        structureExtractionMissionService.delete(missionId);
        createdMissionIds.remove(missionId);

        StructureExtractionMission deleted = structureExtractionMissionService.findById(missionId);
        assertNull(deleted);
    }

    /**
     * 测试删除失败的任务
     */
    @Test
    public void deleteFailedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(UUID.randomUUID().toString());
        mission.failure("Test failure for deletion");

        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
        assertNotNull(saved);
        assertEquals(MissionStatus.ERROR, saved.getStatus());

        structureExtractionMissionService.delete(missionId);
        createdMissionIds.remove(missionId);

        StructureExtractionMission deleted = structureExtractionMissionService.findById(missionId);
        assertNull(deleted);
    }

    /**
     * 测试重复删除同一任务（幂等性）
     */
    @Test
    public void deleteMissionMultipleTimesTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        assertDoesNotThrow(() -> structureExtractionMissionService.delete(missionId));
        createdMissionIds.remove(missionId);

        StructureExtractionMission deletedOnce = structureExtractionMissionService.findById(missionId);
        assertNull(deletedOnce);

        assertDoesNotThrow(() -> structureExtractionMissionService.delete(missionId));
        assertDoesNotThrow(() -> structureExtractionMissionService.delete(missionId));
    }

    /**
     * 测试边界情况 - 空字符串任务ID
     */
    @Test
    public void deleteEmptyStringIdTest() {
        assertThrows(IllegalArgumentException.class, () -> structureExtractionMissionService.delete(""),
                "空字符串任务ID应该抛出IllegalArgumentException");
    }
}
