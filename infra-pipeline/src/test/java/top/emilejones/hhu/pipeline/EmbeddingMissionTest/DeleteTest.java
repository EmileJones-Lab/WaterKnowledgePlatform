package top.emilejones.hhu.pipeline.EmbeddingMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.EmbeddingMissionMapper;
import top.emilejones.hhu.pipeline.mapper.OcrMissionMapper;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量化任务删除测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {

    @Autowired
    private EmbeddingMissionService embeddingMissionService;
    @Autowired
    private EmbeddingMissionMapper embeddingMissionMapper;

    private final List<String> createdMissionIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        createdMissionIds.forEach(id -> embeddingMissionMapper.hardDelete(id));
    }

    /**
     * 测试删除已存在的向量化任务
     */
    @Test
    public void deleteExistingMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        // 验证任务存在
        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission, "任务应该存在");

        // 删除任务
        embeddingMissionService.delete(missionId);

        // 验证任务已被删除
        EmbeddingMission deletedMission = embeddingMissionService.findById(missionId);
        assertNull(deletedMission, "删除后任务应该不存在");
    }

    /**
     * 测试删除不存在的向量化任务（幂等性测试）
     */
    @Test
    public void deleteNonExistingMissionTest() {
        String nonExistingMissionId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> {
            embeddingMissionService.delete(nonExistingMissionId);
        }, "删除不存在的任务不应该抛出异常");

        // 验证确实不存在
        EmbeddingMission mission = embeddingMissionService.findById(nonExistingMissionId);
        assertNull(mission, "不存在的任务ID应该返回null");
    }

    /**
     * 测试删除已完成的任务
     */
    @Test
    public void deleteCompletedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.start(UUID.randomUUID().toString());
        embeddingMission.success(UUID.randomUUID().toString());

        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且已完成
        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertTrue(savedMission.isCompleted());
        assertTrue(savedMission.isSuccess());

        // 删除任务
        embeddingMissionService.delete(missionId);

        // 验证任务已被删除
        EmbeddingMission deletedMission = embeddingMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试删除失败的任务
     */
    @Test
    public void deleteFailedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.start(UUID.randomUUID().toString());
        embeddingMission.failure("向量化服务连接失败");

        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且已失败
        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertTrue(savedMission.isCompleted());
        assertFalse(savedMission.isSuccess());
        assertEquals(MissionStatus.ERROR, savedMission.getStatus());

        // 删除任务
        embeddingMissionService.delete(missionId);

        // 验证任务已被删除
        EmbeddingMission deletedMission = embeddingMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试删除运行中的任务
     */
    @Test
    public void deleteRunningMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMission.start(UUID.randomUUID().toString());

        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且正在运行
        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.RUNNING, savedMission.getStatus());
        assertNotNull(savedMission.getStartTime());
        assertNull(savedMission.getEndTime());

        // 删除任务
        embeddingMissionService.delete(missionId);

        // 验证任务已被删除
        EmbeddingMission deletedMission = embeddingMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试重复删除同一任务（幂等性测试）
     */
    @Test
    public void deleteMissionMultipleTimesTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        // 验证任务存在
        EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
        assertNotNull(savedMission);

        // 第一次删除
        assertDoesNotThrow(() -> {
            embeddingMissionService.delete(missionId);
        });

        // 验证任务已被删除
        EmbeddingMission deletedOnce = embeddingMissionService.findById(missionId);
        assertNull(deletedOnce);

        // 第二次删除，应该不抛出异常
        assertDoesNotThrow(() -> {
            embeddingMissionService.delete(missionId);
        });

        // 第三次删除，应该不抛出异常
        assertDoesNotThrow(() -> {
            embeddingMissionService.delete(missionId);
        });

        // 验证任务仍然不存在
        EmbeddingMission finallyDeleted = embeddingMissionService.findById(missionId);
        assertNull(finallyDeleted);
    }

    /**
     * 测试删除任务后对其他查询的影响
     */
    @Test
    public void deleteMissionAndQueryTest() {
        String sourceDocumentId = UUID.randomUUID().toString();

        // 创建多个任务
        String missionId1 = UUID.randomUUID().toString();
        String missionId2 = UUID.randomUUID().toString();
        String missionId3 = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission1 = EmbeddingMission.Companion.create(missionId1, sourceDocumentId);
        EmbeddingMission embeddingMission2 = EmbeddingMission.Companion.create(missionId2, sourceDocumentId);
        EmbeddingMission embeddingMission3 = EmbeddingMission.Companion.create(missionId3, sourceDocumentId);

        embeddingMission1.start(UUID.randomUUID().toString());
        embeddingMission1.success(UUID.randomUUID().toString());

        embeddingMissionService.save(embeddingMission1);
        embeddingMissionService.save(embeddingMission2);
        embeddingMissionService.save(embeddingMission3);

        createdMissionIds.add(missionId1);
        createdMissionIds.add(missionId2);
        createdMissionIds.add(missionId3);

        // 验证找到3个任务
        var missionsBySource = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);
        assertEquals(3, missionsBySource.size());

        // 删除中间的任务
        embeddingMissionService.delete(missionId2);

        // 验证删除后的状态
        EmbeddingMission deletedMission = embeddingMissionService.findById(missionId2);
        assertNull(deletedMission);

        EmbeddingMission remainingMission1 = embeddingMissionService.findById(missionId1);
        assertNotNull(remainingMission1);

        EmbeddingMission remainingMission3 = embeddingMissionService.findById(missionId3);
        assertNotNull(remainingMission3);

        // 验证按源文档查询现在只返回2个任务
        var remainingMissionsBySource = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);
        assertEquals(2, remainingMissionsBySource.size());

        // 验证剩余任务的ID
        boolean hasMission1 = false;
        boolean hasMission3 = false;
        for (var mission : remainingMissionsBySource) {
            if (mission.getId().equals(missionId1)) hasMission1 = true;
            if (mission.getId().equals(missionId3)) hasMission3 = true;
        }
        assertTrue(hasMission1, "应该保留任务1");
        assertTrue(hasMission3, "应该保留任务3");
    }

    /**
     * 测试边界情况 - 空字符串任务ID
     */
    @Test
    public void deleteEmptyStringIdTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingMissionService.delete("");
        }, "空字符串任务ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试边界情况 - null任务ID
     */
    @Test
    public void deleteNullIdTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingMissionService.delete(null);
        }, "null任务ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试删除任务对批量查询的影响
     */
    @Test
    public void deleteMissionAndBatchQueryTest() {
        String[] sourceDocumentIds = {
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        };
        List<String> sourceDocIdList = List.of(sourceDocumentIds);

        // 为每个源文档创建任务
        String missionId1 = UUID.randomUUID().toString();
        String missionId2 = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission1 = EmbeddingMission.Companion.create(missionId1, sourceDocumentIds[0]);
        EmbeddingMission embeddingMission2 = EmbeddingMission.Companion.create(missionId2, sourceDocumentIds[1]);

        embeddingMissionService.save(embeddingMission1);
        embeddingMissionService.save(embeddingMission2);

        createdMissionIds.add(missionId1);
        createdMissionIds.add(missionId2);

        // 验证批量查询找到2个任务
        var initialResults = embeddingMissionService.findBatchBySourceDocumentId(sourceDocIdList);
        assertEquals(2, initialResults.size());
        assertEquals(1, initialResults.get(0).size());
        assertEquals(1, initialResults.get(1).size());

        // 删除第一个任务
        embeddingMissionService.delete(missionId1);

        // 验证删除后的批量查询结果
        var afterDeleteResults = embeddingMissionService.findBatchBySourceDocumentId(sourceDocIdList);
        assertEquals(2, afterDeleteResults.size());
        assertEquals(0, afterDeleteResults.get(0).size()); // 第一个源文档现在没有任务
        assertEquals(1, afterDeleteResults.get(1).size()); // 第二个源文档仍有任务
    }
}