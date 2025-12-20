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
 * 结构化抽取任务根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {

    @Autowired
    private StructureExtractionMissionService structureExtractionMissionService;
    @Autowired
    private StructureExtractionMissionMapper structureExtractionMissionMapper;

    private final List<String> createdMissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        createdMissionIds.forEach(id -> structureExtractionMissionMapper.hardDelete(id));
    }

    /**
     * 测试根据源文档ID查找任务 - 单个任务
     */
    @Test
    public void findBySourceDocumentIdSingleTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        String missionId = UUID.randomUUID().toString();

        StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
        structureExtractionMissionService.save(mission);
        createdMissionIds.add(missionId);

        List<StructureExtractionMission> missions = structureExtractionMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(missions);
        assertEquals(1, missions.size());
        StructureExtractionMission found = missions.get(0);
        assertEquals(missionId, found.getId());
        assertEquals(sourceDocumentId, found.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, found.getStatus());
    }

    /**
     * 测试根据源文档ID查找任务 - 多个任务
     */
    @Test
    public void findBySourceDocumentIdMultipleTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        int missionCount = 5;

        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceDocumentId);
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);
            sleep();
        }

        List<StructureExtractionMission> missions = structureExtractionMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(missions);
        assertEquals(missionCount, missions.size());

        for (int i = 0; i < missions.size() - 1; i++) {
            StructureExtractionMission current = missions.get(i);
            StructureExtractionMission next = missions.get(i + 1);
            assertTrue(current.getCreateTime().isAfter(next.getCreateTime()) ||
                    current.getCreateTime().equals(next.getCreateTime()),
                    "任务应该按创建时间倒序排列");
        }
        missions.forEach(m -> assertEquals(sourceDocumentId, m.getSourceDocumentId()));
    }

    /**
     * 测试查找不存在源文档ID的任务
     */
    @Test
    public void findBySourceDocumentIdNonExistingTest() {
        List<StructureExtractionMission> missions = structureExtractionMissionService.findBySourceDocumentId(UUID.randomUUID().toString());
        assertNotNull(missions);
        assertTrue(missions.isEmpty(), "不存在的源文档ID应该返回空列表");
    }

    /**
     * 测试查找包含不同状态的任务
     */
    @Test
    public void findBySourceDocumentIdMixedStatusTest() {
        String sourceDocumentId = UUID.randomUUID().toString();

        String[] missionIds = new String[4];
        MissionStatus[] expected = {
                MissionStatus.CREATED,
                MissionStatus.PENDING,
                MissionStatus.RUNNING,
                MissionStatus.SUCCESS
        };

        for (int i = 0; i < missionIds.length; i++) {
            missionIds[i] = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionIds[i], sourceDocumentId);
            switch (i) {
                case 2 -> {
                    mission.start(UUID.randomUUID().toString());
                }
                case 3 -> {
                    mission.start(UUID.randomUUID().toString());
                    mission.success(UUID.randomUUID().toString());
                }
            }
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionIds[i]);
            sleep();
        }

        List<StructureExtractionMission> missions = structureExtractionMissionService.findBySourceDocumentId(sourceDocumentId);
        assertNotNull(missions);
        assertEquals(4, missions.size());

        for (StructureExtractionMission mission : missions) {
            assertEquals(sourceDocumentId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试查找不同源文档ID的任务
     */
    @Test
    public void findBySourceDocumentIdDifferentSourcesTest() {
        String[] sourceIds = {
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        };

        for (String sourceId : sourceIds) {
            String missionId = UUID.randomUUID().toString();
            StructureExtractionMission mission = StructureExtractionMission.Companion.create(missionId, sourceId);
            structureExtractionMissionService.save(mission);
            createdMissionIds.add(missionId);
        }

        for (String sourceId : sourceIds) {
            List<StructureExtractionMission> missions = structureExtractionMissionService.findBySourceDocumentId(sourceId);
            assertNotNull(missions);
            assertEquals(1, missions.size());
            assertEquals(sourceId, missions.get(0).getSourceDocumentId());
        }
    }

    /**
     * 测试边界情况 - 空字符串源文档ID
     */
    @Test
    public void findBySourceDocumentIdEmptyStringTest() {
        assertThrows(IllegalArgumentException.class, () -> structureExtractionMissionService.findBySourceDocumentId(""),
                "空字符串源文档ID应该抛出IllegalArgumentException");
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
