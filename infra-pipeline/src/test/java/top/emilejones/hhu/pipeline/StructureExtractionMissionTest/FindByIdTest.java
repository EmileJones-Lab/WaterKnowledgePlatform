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
 * 结构化抽取任务根据ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByIdTest {

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
     * 测试根据ID查找已存在的任务
     */
    @Test
    public void findExistingMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission found = structureExtractionMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(missionId, found.getId());
        assertEquals(sourceDocumentId, found.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, found.getStatus());
    }

    /**
     * 测试查找不存在的任务
     */
    @Test
    public void findNonExistingMissionByIdTest() {
        StructureExtractionMission mission = structureExtractionMissionService.findById(UUID.randomUUID().toString());
        assertNull(mission, "不存在的任务ID应该返回null");
    }

    /**
     * 测试查找运行中任务
     */
    @Test
    public void findRunningMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String processedDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(processedDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission found = structureExtractionMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(MissionStatus.RUNNING, found.getStatus());
        assertEquals(processedDocumentId, found.getProcessedDocumentId());
        assertNotNull(found.getStartTime());
    }

    /**
     * 测试查找成功任务
     */
    @Test
    public void findSuccessMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(UUID.randomUUID().toString());
        mission.success(UUID.randomUUID().toString());
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission found = structureExtractionMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(MissionStatus.SUCCESS, found.getStatus());
        assertTrue(found.isSuccess());
        assertNotNull(found.getEndTime());
    }

    /**
     * 测试查找失败任务
     */
    @Test
    public void findFailedMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        mission.preparedToExecution();
        mission.start(UUID.randomUUID().toString());
        mission.failure("Test failure");
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        StructureExtractionMission found = structureExtractionMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(MissionStatus.ERROR, found.getStatus());
        assertTrue(found.isCompleted());
        assertFalse(found.isSuccess());
        assertNotNull(found.getEndTime());
    }
}
