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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 结构化抽取任务批量根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBatchBySourceDocumentIdTest {

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
     * 测试批量查找 - 每个源文档ID对应一个任务
     */
    @Test
    public void findBatchBySourceDocumentIdSingleMissionTest() {
        List<String> sourceDocumentIds = Arrays.asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        for (String sourceDocId : sourceDocumentIds) {
            String missionId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocId);
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);
        }

        List<List<StructureExtractionMission>> result = structureExtractionMissionService.findBySourceDocumentIdList(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(sourceDocumentIds.size(), result.size());

        for (int i = 0; i < result.size(); i++) {
            List<StructureExtractionMission> missions = result.get(i);
            assertNotNull(missions);
            assertEquals(1, missions.size());
            assertEquals(sourceDocumentIds.get(i), missions.get(0).getSourceDocumentId());
        }
    }

    /**
     * 测试批量查找 - 某些源文档ID有多个任务
     */
    @Test
    public void findBatchBySourceDocumentIdMultipleMissionsTest() {
        List<String> sourceDocumentIds = Arrays.asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        for (int i = 0; i < 3; i++) {
            String missionId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentIds.get(0));
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);
            sleep();
        }

        String singleMissionId = UUID.randomUUID().toString();
        StructureExtractionMission singleMission = StructureExtractionMission.Companion.create(singleMissionId, sourceDocumentIds.get(1));
        structureExtractionMissionService.save(singleMission);
        createdMissionIds.add(singleMissionId);

        for (int i = 0; i < 2; i++) {
            String missionId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentIds.get(2));
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);
            sleep();
        }

        List<List<StructureExtractionMission>> result = structureExtractionMissionService.findBySourceDocumentIdList(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(3, result.get(0).size());
        assertEquals(1, result.get(1).size());
        assertEquals(2, result.get(2).size());

        for (List<StructureExtractionMission> missions : result) {
            for (int i = 0; i < missions.size() - 1; i++) {
                StructureExtractionMission current = missions.get(i);
                StructureExtractionMission next = missions.get(i + 1);
                assertTrue(current.getCreateTime().isAfter(next.getCreateTime()) ||
                        current.getCreateTime().equals(next.getCreateTime()),
                        "任务应该按创建时间倒序排列");
            }
        }
    }

    /**
     * 测试批量查找 - 包含不存在的源文档ID
     */
    @Test
    public void findBatchBySourceDocumentIdWithNonExistingTest() {
        List<String> sourceDocumentIds = Arrays.asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        String missionId1 = UUID.randomUUID().toString();
        StructureExtractionMission mission1 = StructureExtractionMission.Companion.create(missionId1, sourceDocumentIds.get(0));
        structureExtractionMissionService.save(mission1);
        createdMissionIds.add(missionId1);

        String missionId2 = UUID.randomUUID().toString();
        StructureExtractionMission mission2 = StructureExtractionMission.Companion.create(missionId2, sourceDocumentIds.get(2));
        structureExtractionMissionService.save(mission2);
        createdMissionIds.add(missionId2);

        List<List<StructureExtractionMission>> result = structureExtractionMissionService.findBySourceDocumentIdList(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).size());
        assertTrue(result.get(1).isEmpty());
        assertEquals(1, result.get(2).size());
    }

    /**
     * 测试批量查找 - 空列表输入
     */
    @Test
    public void findBatchBySourceDocumentIdEmptyListTest() {
        List<List<StructureExtractionMission>> result = structureExtractionMissionService.findBySourceDocumentIdList(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty(), "空列表输入应该返回空列表");
    }

    /**
     * 测试批量查找 - 重复的源文档ID
     */
    @Test
    public void findBatchBySourceDocumentIdWithDuplicatesTest() {
        String sourceDocId = UUID.randomUUID().toString();
        List<String> sourceDocumentIds = Arrays.asList(sourceDocId, sourceDocId, sourceDocId);

        String missionId1 = UUID.randomUUID().toString();
        StructureExtractionMission mission1 = StructureExtractionMission.Companion.create(missionId1, sourceDocId);
        structureExtractionMissionService.save(mission1);
        createdMissionIds.add(missionId1);

        sleep();

        String missionId2 = UUID.randomUUID().toString();
        StructureExtractionMission mission2 = StructureExtractionMission.Companion.create(missionId2, sourceDocId);
        structureExtractionMissionService.save(mission2);
        createdMissionIds.add(missionId2);

        List<List<StructureExtractionMission>> result = structureExtractionMissionService.findBySourceDocumentIdList(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(3, result.size());
        for (List<StructureExtractionMission> missions : result) {
            assertEquals(2, missions.size(), "每个结果应该包含2个任务");
            for (StructureExtractionMission mission : missions) {
                assertEquals(sourceDocId, mission.getSourceDocumentId());
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
