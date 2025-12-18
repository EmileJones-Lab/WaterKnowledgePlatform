package top.emilejones.hhu.pipeline.EmbeddingMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMissionResult;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.EmbeddingMissionMapper;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量化任务根据ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByIdTest {

    @Autowired
    private EmbeddingMissionService embeddingMissionService;
    @Autowired
    private EmbeddingMissionMapper embeddingMissionMapper;

    private final List<String> createdMissionIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUp() {
        embeddingMissionMapper.truncateTable();
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        embeddingMissionMapper.truncateTable();
    }


    /**
     * 测试根据ID查找已存在的任务
     */
    @Test
    public void findExistingMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission found = embeddingMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(missionId, found.getId());
        assertEquals(sourceDocumentId, found.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, found.getStatus());
        assertNull(found.getFileNodeId()); // 初始状态下fileNodeId为null
    }

    /**
     * 测试查找不存在的任务
     */
    @Test
    public void findNonExistingMissionByIdTest() {
        EmbeddingMission mission = embeddingMissionService.findById(UUID.randomUUID().toString());
        assertNull(mission, "不存在的任务ID应该返回null");
    }

    /**
     * 测试查找运行中任务
     */
    @Test
    public void findRunningMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileNodeId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.preparedToExecution();
        embeddingMission.start(fileNodeId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission found = embeddingMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(MissionStatus.RUNNING, found.getStatus());
        assertEquals(fileNodeId, found.getFileNodeId());
        assertNotNull(found.getStartTime());
    }

    /**
     * 测试查找成功任务
     */
    @Test
    public void findSuccessMissionByIdTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.preparedToExecution();
        embeddingMission.start(UUID.randomUUID().toString());
        embeddingMission.success(UUID.randomUUID().toString());
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission found = embeddingMissionService.findById(missionId);
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

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.preparedToExecution();
        embeddingMission.start(UUID.randomUUID().toString());
        embeddingMission.failure("向量化处理失败");
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission found = embeddingMissionService.findById(missionId);
        assertNotNull(found);
        assertEquals(MissionStatus.ERROR, found.getStatus());
        assertTrue(found.isCompleted());
        assertFalse(found.isSuccess());
        assertNotNull(found.getEndTime());
    }
}