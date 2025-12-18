package top.emilejones.hhu.pipeline.StructureExtractionMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMission;
import top.emilejones.hhu.domain.pipeline.splitter.StructureExtractionMissionResult;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.StructureExtractionMissionMapper;
import top.emilejones.hhu.pipeline.services.StructureExtractionMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结构化抽取任务保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {

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
     * 测试保存单个结构化抽取任务
     */
    @Test
    public void saveTest() {
        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);

            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);

            StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
            assertNotNull(saved, "保存的任务应该能够被找到");
            assertEquals(missionId, saved.getId());
            assertEquals(sourceDocumentId, saved.getSourceDocumentId());
            assertEquals(MissionStatus.CREATED, saved.getStatus());
        }
    }

    /**
     * 测试更新已存在的结构化抽取任务
     */
    @Test
    public void updateTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission saved = structureExtractionMissionService.findById(missionId);
        assertNotNull(saved);
        assertEquals(MissionStatus.CREATED, saved.getStatus());

        String processedDocumentId = UUID.randomUUID().toString();
        saved.preparedToExecution();
        saved.start(processedDocumentId);
        structureExtractionMissionService.save(saved);

        StructureExtractionMission running = structureExtractionMissionService.findById(missionId);
        assertNotNull(running);
        assertEquals(MissionStatus.RUNNING, running.getStatus());
        assertEquals(processedDocumentId, running.getProcessedDocumentId());
        assertNotNull(running.getStartTime());

        String fileNodeId = UUID.randomUUID().toString();
        running.success(fileNodeId);
        structureExtractionMissionService.save(running);

        StructureExtractionMission successMission = structureExtractionMissionService.findById(missionId);
        assertNotNull(successMission);
        assertEquals(MissionStatus.SUCCESS, successMission.getStatus());
        assertTrue(successMission.isSuccess());
        assertNotNull(successMission.getEndTime());
        StructureExtractionMissionResult.Success result = successMission.getSuccessResult();
        assertEquals(fileNodeId, result.getFileNodeId());
    }

    /**
     * 测试任务失败状态的保存
     */
    @Test
    public void saveFailedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String processedDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(processedDocumentId);

        String errorMessage = "Structure extraction failed due to invalid structure";
        mission.failure(errorMessage);

        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission savedMission = structureExtractionMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.ERROR, savedMission.getStatus());
        assertTrue(savedMission.isCompleted());
        assertFalse(savedMission.isSuccess());
        assertNotNull(savedMission.getEndTime());

        StructureExtractionMissionResult.Failure failureResult = savedMission.getFailureResult();
        assertEquals(errorMessage, failureResult.getErrorMessage());
    }
}
