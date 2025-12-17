package top.emilejones.hhu.pipeline.EmbeddingMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.embedding.EmbeddingMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.services.EmbeddingMissionService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量化任务根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {

    @Autowired
    private EmbeddingMissionService embeddingMissionService;

    private final List<String> createdMissionIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        for (String missionId : createdMissionIds) {
            try {
                embeddingMissionService.delete(missionId);
            } catch (Exception ignored) {
            }
        }
        createdMissionIds.clear();
    }

    /**
     * 测试根据源文档ID查找任务 - 单个任务
     */
    @Test
    public void findBySourceDocumentIdSingleTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        String missionId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(foundMissions);
        assertEquals(1, foundMissions.size(), "应该找到1个任务");

        EmbeddingMission foundMission = foundMissions.get(0);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, foundMission.getStatus());
        assertNull(foundMission.getFileNodeId()); // 初始状态下fileNodeId为null
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

            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionId);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(foundMissions);
        assertEquals(missionCount, foundMissions.size(), "应该找到" + missionCount + "个任务");

        // 验证任务按创建时间倒序排列
        for (int i = 0; i < foundMissions.size() - 1; i++) {
            EmbeddingMission current = foundMissions.get(i);
            EmbeddingMission next = foundMissions.get(i + 1);

            assertTrue(current.getCreateTime().isAfter(next.getCreateTime()) ||
                     current.getCreateTime().equals(next.getCreateTime()),
                    "任务应该按创建时间倒序排列");
        }

        // 验证所有任务都有正确的源文档ID
        for (EmbeddingMission mission : foundMissions) {
            assertEquals(sourceDocumentId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试查找不存在源文档ID的任务
     */
    @Test
    public void findBySourceDocumentIdNonExistingTest() {
        String nonExistingSourceDocId = UUID.randomUUID().toString();

        List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(nonExistingSourceDocId);

        assertNotNull(foundMissions);
        assertTrue(foundMissions.isEmpty(), "不存在的源文档ID应该返回空列表");
    }

    /**
     * 测试查找包含不同状态的任务
     */
    @Test
    public void findBySourceDocumentIdMixedStatusTest() {
        String sourceDocumentId = UUID.randomUUID().toString();

        // 创建不同状态的任务
        String[] missionIds = new String[5];

        for (int i = 0; i < 5; i++) {
            missionIds[i] = UUID.randomUUID().toString();
            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionIds[i], sourceDocumentId);

            switch (i) {
                case 0:
                    // 保持CREATED状态
                    break;
                case 1:
                    embeddingMission.preparedToExecution();
                    break;
                case 2:
                    embeddingMission.preparedToExecution();
                    embeddingMission.start(UUID.randomUUID().toString());
                    break;
                case 3:
                    embeddingMission.preparedToExecution();
                    embeddingMission.start(UUID.randomUUID().toString());
                    embeddingMission.success(UUID.randomUUID().toString());
                    break;
                case 4:
                    embeddingMission.preparedToExecution();
                    embeddingMission.start(UUID.randomUUID().toString());
                    embeddingMission.failure("向量化处理失败");
                    break;
            }

            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionIds[i]);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(foundMissions);
        assertEquals(5, foundMissions.size(), "应该找到5个任务");

        for (EmbeddingMission mission : foundMissions) {
            assertEquals(sourceDocumentId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试查找不同源文档ID的任务
     */
    @Test
    public void findBySourceDocumentIdDifferentSourcesTest() {
        String[] sourceDocumentIds = {
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        };

        for (int i = 0; i < sourceDocumentIds.length; i++) {
            String missionId = UUID.randomUUID().toString();
            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentIds[i]);
            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionId);
        }

        for (String sourceDocId : sourceDocumentIds) {
            List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(sourceDocId);

            assertNotNull(foundMissions);
            assertEquals(1, foundMissions.size(), "源文档 " + sourceDocId + " 应该找到1个任务");

            EmbeddingMission mission = foundMissions.get(0);
            assertEquals(sourceDocId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试边界情况 - 空字符串源文档ID
     */
    @Test
    public void findBySourceDocumentIdEmptyStringTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingMissionService.findBySourceDocumentId("");
        }, "空字符串源文档ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试边界情况 - null源文档ID
     */
    @Test
    public void findBySourceDocumentIdNullTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            embeddingMissionService.findBySourceDocumentId(null);
        }, "null源文档ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试查找包含失败任务的情况
     */
    @Test
    public void findBySourceDocumentIdWithFailedTest() {
        String sourceDocumentId = UUID.randomUUID().toString();

        // 创建成功的任务
        String successMissionId = UUID.randomUUID().toString();
        EmbeddingMission successMission = EmbeddingMission.Companion.create(successMissionId, sourceDocumentId);
        successMission.preparedToExecution();
        successMission.start(UUID.randomUUID().toString());
        successMission.success(UUID.randomUUID().toString());
        embeddingMissionService.save(successMission);
        createdMissionIds.add(successMissionId);

        // 创建失败的任务
        String failedMissionId = UUID.randomUUID().toString();
        EmbeddingMission failedMission = EmbeddingMission.Companion.create(failedMissionId, sourceDocumentId);
        failedMission.preparedToExecution();
        failedMission.start(UUID.randomUUID().toString());
        failedMission.failure("向量化服务连接失败");
        embeddingMissionService.save(failedMission);
        createdMissionIds.add(failedMissionId);

        List<EmbeddingMission> foundMissions = embeddingMissionService.findBySourceDocumentId(sourceDocumentId);

        assertNotNull(foundMissions);
        assertEquals(2, foundMissions.size(), "应该找到2个任务");

        boolean hasSuccess = false;
        boolean hasFailure = false;

        for (EmbeddingMission mission : foundMissions) {
            if (mission.isSuccess()) {
                hasSuccess = true;
            } else if (mission.getStatus() == MissionStatus.ERROR) {
                hasFailure = true;
            }
        }

        assertTrue(hasSuccess, "应该包含成功的任务");
        assertTrue(hasFailure, "应该包含失败的任务");
    }
}