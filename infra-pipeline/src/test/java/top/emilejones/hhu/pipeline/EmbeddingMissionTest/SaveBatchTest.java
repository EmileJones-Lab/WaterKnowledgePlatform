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
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量化任务批量保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveBatchTest {

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
     * 测试批量保存多个向量化任务
     */
    @Test
    public void saveBatchTest() {
        List<EmbeddingMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
            missionList.add(embeddingMission);
            missionIds.add(missionId);
        }

        embeddingMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        for (String missionId : missionIds) {
            EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
            assertNotNull(savedMission, "任务ID: " + missionId + " 应该能够被找到");
            assertEquals(missionId, savedMission.getId());
            assertEquals(MissionStatus.CREATED, savedMission.getStatus());
            assertNull(savedMission.getFileNodeId()); // 初始状态下fileNodeId为null
        }
    }

    /**
     * 测试批量保存空列表
     */
    @Test
    public void saveEmptyBatchTest() {
        // 保存空列表应该不会抛出异常
        assertDoesNotThrow(() -> {
            embeddingMissionService.saveBatch(new ArrayList<>());
        });
    }

    /**
     * 测试批量保存不同状态的任务
     */
    @Test
    public void saveMixedStatusBatchTest() {
        List<EmbeddingMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);

            switch (i % 4) {
                case 0:
                    // 保持CREATED状态
                    break;
                case 1:
                    break;
                case 2:
                    embeddingMission.start(UUID.randomUUID().toString());
                    break;
                case 3:
                    embeddingMission.start(UUID.randomUUID().toString());
                    embeddingMission.success(UUID.randomUUID().toString());
                    break;
            }

            missionList.add(embeddingMission);
            missionIds.add(missionId);
        }

        embeddingMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);
            EmbeddingMission savedMission = embeddingMissionService.findById(missionId);
            assertNotNull(savedMission, "任务ID: " + missionId + " 应该能够被找到");

            switch (i % 4) {
                case 0:
                    assertEquals(MissionStatus.CREATED, savedMission.getStatus());
                    break;
                case 1:
                    assertEquals(MissionStatus.PENDING, savedMission.getStatus());
                    break;
                case 2:
                    assertEquals(MissionStatus.RUNNING, savedMission.getStatus());
                    assertNotNull(savedMission.getStartTime());
                    break;
                case 3:
                    assertEquals(MissionStatus.SUCCESS, savedMission.getStatus());
                    assertTrue(savedMission.isSuccess());
                    assertNotNull(savedMission.getEndTime());
                    break;
            }
        }
    }

    /**
     * 测试批量更新任务（upsert语义）
     */
    @Test
    public void saveBatchUpdateTest() {
        List<EmbeddingMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        // 第一阶段：创建并保存任务
        for (int i = 0; i < 5; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
            missionList.add(embeddingMission);
            missionIds.add(missionId);
        }

        embeddingMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        // 第二阶段：更新任务状态
        List<EmbeddingMission> updatedMissionList = new ArrayList<>();
        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);

            EmbeddingMission existingMission = embeddingMissionService.findById(missionId);
            assertNotNull(existingMission);

            existingMission.start(UUID.randomUUID().toString());

            if (i % 2 == 0) {
                // 一半成功
                existingMission.success(UUID.randomUUID().toString());
            } else {
                // 一半失败
                existingMission.failure("向量化处理失败");
            }

            updatedMissionList.add(existingMission);
        }

        // 批量更新任务
        embeddingMissionService.saveBatch(updatedMissionList);

        // 验证更新结果
        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);
            EmbeddingMission finalMission = embeddingMissionService.findById(missionId);
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