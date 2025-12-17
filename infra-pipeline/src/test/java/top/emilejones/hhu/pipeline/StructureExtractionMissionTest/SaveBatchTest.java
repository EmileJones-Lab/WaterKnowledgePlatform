package top.emilejones.hhu.pipeline.StructureExtractionMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结构化抽取任务批量保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveBatchTest {

    @Autowired
    private StructureExtractionMissionService structureExtractionMissionService;

    private final List<String> createdMissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        for (String missionId : createdMissionIds) {
            try {
                structureExtractionMissionService.delete(missionId);
            } catch (Exception ignored) {
            }
        }
        createdMissionIds.clear();
    }

    /**
     * 测试批量保存多个结构化抽取任务
     */
    @Test
    public void saveBatchTest() {
        List<StructureExtractionMission> missions = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
            missions.add(mission);
            missionIds.add(missionId);
        }

        structureExtractionMissionService.saveBatch(missions);
        createdMissionIds.addAll(missionIds);

        for (String missionId : missionIds) {
            StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
            assertNotNull(saved, "任务ID: " + missionId + " 应该能够被找到");
            assertEquals(MissionStatus.CREATED, saved.getStatus());
        }
    }

    /**
     * 测试批量保存空列表
     */
    @Test
    public void saveEmptyBatchTest() {
        assertDoesNotThrow(() -> structureExtractionMissionService.saveBatch(new ArrayList<>()));
    }

    /**
     * 测试批量保存不同状态的任务
     */
    @Test
    public void saveMixedStatusBatchTest() {
        List<StructureExtractionMission> missions = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);

            switch (i % 4) {
                case 0 -> {
                    // CREATED
                }
                case 1 -> mission.preparedToExecution();
                case 2 -> {
                    mission.preparedToExecution();
                    mission.start(UUID.randomUUID().toString());
                }
                case 3 -> {
                    mission.preparedToExecution();
                    String processedId = UUID.randomUUID().toString();
                    mission.start(processedId);
                    mission.success(UUID.randomUUID().toString());
                }
            }

            missions.add(mission);
            missionIds.add(missionId);
        }

        structureExtractionMissionService.saveBatch(missions);
        createdMissionIds.addAll(missionIds);

        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);
            StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
            assertNotNull(saved);
            switch (i % 4) {
                case 0 -> assertEquals(MissionStatus.CREATED, saved.getStatus());
                case 1 -> assertEquals(MissionStatus.PENDING, saved.getStatus());
                case 2 -> {
                    assertEquals(MissionStatus.RUNNING, saved.getStatus());
                    assertNotNull(saved.getStartTime());
                }
                case 3 -> {
                    assertEquals(MissionStatus.SUCCESS, saved.getStatus());
                    assertTrue(saved.isSuccess());
                    assertNotNull(saved.getEndTime());
                }
            }
        }
    }

    /**
     * 测试批量更新任务（upsert）
     */
    @Test
    public void saveBatchUpdateTest() {
        List<StructureExtractionMission> missions = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
            missions.add(mission);
            missionIds.add(missionId);
        }

        structureExtractionMissionService.saveBatch(missions);
        createdMissionIds.addAll(missionIds);

        List<StructureExtractionMission> updated = new ArrayList<>();
        for (int i = 0; i < missionIds.size(); i++) {
            StructureExtractionMission existing = structureExtractionMissionService.findById(missionIds.get(i));
            assertNotNull(existing);

            existing.preparedToExecution();
            existing.start(UUID.randomUUID().toString());

            if (i % 2 == 0) {
                existing.success(UUID.randomUUID().toString());
            } else {
                existing.failure("Processing failed for test");
            }
            updated.add(existing);
        }

        structureExtractionMissionService.saveBatch(updated);

        for (int i = 0; i < missionIds.size(); i++) {
            StructureExtractionMission finalMission = structureExtractionMissionService.findById(missionIds.get(i));
            assertNotNull(finalMission);
            if (i % 2 == 0) {
                assertEquals(MissionStatus.SUCCESS, finalMission.getStatus());
                assertTrue(finalMission.isSuccess());
            } else {
                assertEquals(MissionStatus.ERROR, finalMission.getStatus());
                assertFalse(finalMission.isSuccess());
            }
            assertNotNull(finalMission.getEndTime());
        }
    }
}
