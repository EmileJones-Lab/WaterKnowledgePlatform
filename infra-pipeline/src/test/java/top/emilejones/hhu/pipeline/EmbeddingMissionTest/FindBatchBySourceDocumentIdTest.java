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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量化任务批量根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBatchBySourceDocumentIdTest {

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
     * 测试批量查找单个源文档ID的任务
     */
    @Test
    public void findBatchBySourceDocumentIdSingleMissionTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        List<String> sourceDocumentIds = List.of(sourceDocumentId);

        String missionId = UUID.randomUUID().toString();

        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(1, result.size(), "应该返回1个结果列表");

        List<EmbeddingMission> missions = result.get(0);
        assertEquals(1, missions.size(), "应该找到1个任务");

        EmbeddingMission foundMission = missions.get(0);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, foundMission.getStatus());
        assertNull(foundMission.getFileNodeId()); // 初始状态下fileNodeId为null
    }

    /**
     * 测试批量查找多个源文档ID的任务
     */
    @Test
    public void findBatchBySourceDocumentIdMultipleMissionsTest() {
        String[] sourceDocumentIds = {
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        };
        List<String> sourceDocIdList = Arrays.asList(sourceDocumentIds);

        // 为每个源文档ID创建不同数量的任务
        int[] missionsPerSource = {2, 1, 3};

        for (int i = 0; i < sourceDocumentIds.length; i++) {
            for (int j = 0; j < missionsPerSource[i]; j++) {
                String missionId = UUID.randomUUID().toString();

                EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentIds[i]);
                embeddingMissionService.save(embeddingMission);
                createdMissionIds.add(missionId);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(sourceDocIdList);

        assertNotNull(result);
        assertEquals(sourceDocumentIds.length, result.size(), "应该返回" + sourceDocumentIds.length + "个结果列表");

        // 验证每个源文档ID对应的任务数量
        for (int i = 0; i < result.size(); i++) {
            List<EmbeddingMission> missions = result.get(i);
            assertEquals(missionsPerSource[i], missions.size(),
                    "源文档 " + i + " 应该有 " + missionsPerSource[i] + " 个任务");

            // 验证所有任务都来自正确的源文档
            for (EmbeddingMission mission : missions) {
                assertEquals(sourceDocumentIds[i], mission.getSourceDocumentId());
            }
        }
    }

    /**
     * 测试批量查找包含不存在的源文档ID
     */
    @Test
    public void findBatchBySourceDocumentIdWithNonExistingTest() {
        String existingSourceDocId = UUID.randomUUID().toString();
        String nonExistingSourceDocId = UUID.randomUUID().toString();
        List<String> sourceDocumentIds = Arrays.asList(existingSourceDocId, nonExistingSourceDocId);

        // 只为存在的源文档ID创建任务
        String missionId = UUID.randomUUID().toString();
        EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, existingSourceDocId);
        embeddingMissionService.save(embeddingMission);
        createdMissionIds.add(missionId);

        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        assertNotNull(result);
        assertEquals(2, result.size());

        // 第一个源文档ID应该找到1个任务
        List<EmbeddingMission> firstResult = result.get(0);
        assertEquals(1, firstResult.size());
        assertEquals(missionId, firstResult.get(0).getId());

        // 第二个源文档ID应该返回空列表
        List<EmbeddingMission> secondResult = result.get(1);
        assertNotNull(secondResult);
        assertTrue(secondResult.isEmpty(), "不存在的源文档ID应该返回空列表");
    }

    /**
     * 测试批量查找空列表
     */
    @Test
    public void findBatchBySourceDocumentIdEmptyListTest() {
        List<String> emptyList = Collections.emptyList();

        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(emptyList);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "空输入应该返回空结果");
    }

    /**
     * 测试批量查找复杂场景
     */
    @Test
    public void findBatchBySourceDocumentIdComplexTest() {
        String[] sourceDocumentIds = {
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        };
        List<String> sourceDocIdList = Arrays.asList(sourceDocumentIds);

        // 创建复杂的任务场景
        // 第一个源文档：3个不同状态的任务
        for (int i = 0; i < 3; i++) {
            String missionId = UUID.randomUUID().toString();
            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentIds[0]);

            if (i == 1) {
                embeddingMission.start(UUID.randomUUID().toString());
                embeddingMission.success(UUID.randomUUID().toString());
            } else if (i == 2) {
                embeddingMission.start(UUID.randomUUID().toString());
                embeddingMission.failure("处理失败");
            }

            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionId);
        }

        // 第二个源文档：没有任务（测试空结果）
        // 不创建任务

        // 第三个源文档：1个运行中的任务
        String runningMissionId = UUID.randomUUID().toString();
        EmbeddingMission runningMission = EmbeddingMission.Companion.create(runningMissionId, sourceDocumentIds[2]);
        runningMission.start(UUID.randomUUID().toString());
        embeddingMissionService.save(runningMission);
        createdMissionIds.add(runningMissionId);

        // 第四个源文档：2个已创建的任务
        for (int i = 0; i < 2; i++) {
            String missionId = UUID.randomUUID().toString();
            EmbeddingMission embeddingMission = EmbeddingMission.Companion.create(missionId, sourceDocumentIds[3]);
            embeddingMissionService.save(embeddingMission);
            createdMissionIds.add(missionId);

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(sourceDocIdList);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.size());

        // 验证第一个源文档：3个任务，不同状态
        List<EmbeddingMission> firstResult = result.get(0);
        assertEquals(3, firstResult.size());
        boolean hasCreated = false, hasSuccess = false, hasError = false;
        for (EmbeddingMission mission : firstResult) {
            if (mission.getStatus() == MissionStatus.CREATED) hasCreated = true;
            if (mission.getStatus() == MissionStatus.SUCCESS) hasSuccess = true;
            if (mission.getStatus() == MissionStatus.ERROR) hasError = true;
        }
        assertTrue(hasCreated);
        assertTrue(hasSuccess);
        assertTrue(hasError);

        // 验证第二个源文档：空列表
        List<EmbeddingMission> secondResult = result.get(1);
        assertTrue(secondResult.isEmpty());

        // 验证第三个源文档：1个运行中的任务
        List<EmbeddingMission> thirdResult = result.get(2);
        assertEquals(1, thirdResult.size());
        assertEquals(MissionStatus.RUNNING, thirdResult.get(0).getStatus());

        // 验证第四个源文档：2个已创建的任务
        List<EmbeddingMission> fourthResult = result.get(3);
        assertEquals(2, fourthResult.size());
        for (EmbeddingMission mission : fourthResult) {
            assertEquals(MissionStatus.CREATED, mission.getStatus());
        }
    }

    /**
     * 测试批量查找包含重复的源文档ID
     */
    @Test
    public void findBatchBySourceDocumentIdWithDuplicatesTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        String anotherSourceDocId = UUID.randomUUID().toString();

        // 创建包含重复源文档ID的列表
        List<String> sourceDocumentIds = Arrays.asList(
                sourceDocumentId,
                anotherSourceDocId,
                sourceDocumentId, // 重复
                sourceDocumentId  // 再次重复
        );

        String missionId1 = UUID.randomUUID().toString();
        String missionId2 = UUID.randomUUID().toString();

        // 为第一个源文档ID创建2个任务
        EmbeddingMission embeddingMission1 = EmbeddingMission.Companion.create(missionId1, sourceDocumentId);
        embeddingMissionService.save(embeddingMission1);
        createdMissionIds.add(missionId1);

        EmbeddingMission embeddingMission2 = EmbeddingMission.Companion.create(missionId2, sourceDocumentId);
        embeddingMissionService.save(embeddingMission2);
        createdMissionIds.add(missionId2);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 为第二个源文档ID创建1个任务
        String missionId3 = UUID.randomUUID().toString();
        EmbeddingMission embeddingMission3 = EmbeddingMission.Companion.create(missionId3, anotherSourceDocId);
        embeddingMissionService.save(embeddingMission3);
        createdMissionIds.add(missionId3);

        // 批量查找
        List<List<EmbeddingMission>> result = embeddingMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.size());

        // 验证每个结果都包含正确的任务
        // 位置0和2和3应该都包含相同的2个任务（相同的源文档ID）
        for (int i = 0; i < 4; i++) {
            List<EmbeddingMission> missions = result.get(i);
            if (i == 1) {
                // 第二个源文档ID
                assertEquals(1, missions.size());
                assertEquals(anotherSourceDocId, missions.get(0).getSourceDocumentId());
            } else {
                // 第一个源文档ID（重复3次）
                assertEquals(2, missions.size());
                for (EmbeddingMission mission : missions) {
                    assertEquals(sourceDocumentId, mission.getSourceDocumentId());
                }
            }
        }
    }
}