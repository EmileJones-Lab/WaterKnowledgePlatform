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
 * 向量化任务保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {

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
     * 测试保存单个向量化任务
     */
    @Test
    public void saveTest() {
        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);

            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionId);

            EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
            assertNotNull(savedMission, "保存的任务应该能够被找到");
            assertEquals(missionId, savedMission.getId());
            assertEquals(sourceDocumentId, savedMission.getSourceDocumentId());
            assertEquals(MissionStatus.CREATED, savedMission.getStatus());
            assertNull(savedMission.getFileNodeId()); // 初始状态下fileNodeId为null
        }
    }

    /**
     * 测试更新已存在的向量化任务
     */
    @Test
    public void updateTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission saved = embeddingMissionService.findById(missionId);
        assertNotNull(saved);
        assertEquals(MissionStatus.CREATED, saved.getStatus());

        String fileNodeId = UUID.randomUUID().toString();
        saved.preparedToExecution();
        saved.start(fileNodeId);
        embeddingMissionService.save(saved);

        EmbeddingMission running = embeddingMissionService.findById(missionId);
        assertNotNull(running);
        assertEquals(MissionStatus.RUNNING, running.getStatus());
        assertEquals(fileNodeId, running.getFileNodeId());
        assertNotNull(running.getStartTime());

        String successFileNodeId = UUID.randomUUID().toString();
        running.success(successFileNodeId);
        embeddingMissionService.save(running);

        EmbeddingMission successMission = embeddingMissionService.findById(missionId);
        assertNotNull(successMission);
        assertEquals(MissionStatus.SUCCESS, successMission.getStatus());
        assertTrue(successMission.isSuccess());
        assertNotNull(successMission.getEndTime());
        EmbeddingMissionResult.Success result = successMission.getSuccessResult();
        assertEquals(successFileNodeId, result.getFileNodeId());
    }

    /**
     * 测试任务失败状态的保存
     */
    @Test
    public void saveFailedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileNodeId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.preparedToExecution();
        embeddingMission.start(fileNodeId);

        String errorMessage = "向量化处理失败，Milvus连接超时";
        embeddingMission.failure(errorMessage);

        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.ERROR, savedMission.getStatus());
        assertTrue(savedMission.isCompleted());
        assertFalse(savedMission.isSuccess());
        assertNotNull(savedMission.getEndTime());

        EmbeddingMissionResult.Failure failureResult = savedMission.getFailureResult();
        assertEquals(errorMessage, failureResult.getErrorMessage());
    }
}