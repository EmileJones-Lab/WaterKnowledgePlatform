package top.emilejones.hhu.pipeline.OcrMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.OcrMissionMapper;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务批量根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBatchBySourceDocumentIdTest {

    @Autowired
    private OcrMissionService ocrMissionService;
    @Autowired
    private OcrMissionMapper ocrMissionMapper;

    private List<String> createdMissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ocrMissionMapper.truncateTable();
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        // 测试结束后也可以执行一次，保持数据库清洁
        ocrMissionMapper.truncateTable();
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

        // 为每个源文档ID创建一个任务
        for (String sourceDocId : sourceDocumentIds) {
            String missionId = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocId);
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);
        }

        // 批量查找
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(sourceDocumentIds.size(), result.size(), "返回的结果列表大小应该与输入列表大小一致");

        for (int i = 0; i < result.size(); i++) {
            List<OcrMission> missions = result.get(i);
            String expectedSourceDocId = sourceDocumentIds.get(i);

            assertNotNull(missions, "结果列表不应包含null元素");
            assertEquals(1, missions.size(), "每个源文档ID应该找到1个任务");

            OcrMission mission = missions.get(0);
            assertEquals(expectedSourceDocId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试批量查找 - 某些源文档ID有多个任务
     */
    @Test
    public void findBatchBySourceDocumentIdMultipleMissionsTest() {
        // 第一个源文档有3个任务，第二个有1个，第三个有2个
        List<String> sourceDocumentIds = Arrays.asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        // 为第一个源文档创建3个任务
        for (int i = 0; i < 3; i++) {
            String missionId = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentIds.get(0));
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            // 稍微延迟确保时间不同
            sleep();
        }

        // 为第二个源文档创建1个任务
        String singleMissionId = UUID.randomUUID().toString();
        OcrMission singleMission = OcrMission.Companion.create(singleMissionId, sourceDocumentIds.get(1));
        ocrMissionService.save(singleMission);
        createdMissionIds.add(singleMissionId);

        // 为第三个源文档创建2个任务
        for (int i = 0; i < 2; i++) {
            String missionId = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentIds.get(2));
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        // 批量查找
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(3, result.get(0).size(), "第一个源文档应该找到3个任务");
        assertEquals(1, result.get(1).size(), "第二个源文档应该找到1个任务");
        assertEquals(2, result.get(2).size(), "第三个源文档应该找到2个任务");

        // 验证任务按创建时间倒序排列
        for (List<OcrMission> missions : result) {
            for (int i = 0; i < missions.size() - 1; i++) {
                OcrMission current = missions.get(i);
                OcrMission next = missions.get(i + 1);

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

        // 只为第一个和第三个源文档创建任务
        String missionId1 = UUID.randomUUID().toString();
        OcrMission ocrMission1 = OcrMission.Companion.create(missionId1, sourceDocumentIds.get(0));
        ocrMissionService.save(ocrMission1);
        createdMissionIds.add(missionId1);

        String missionId2 = UUID.randomUUID().toString();
        OcrMission ocrMission2 = OcrMission.Companion.create(missionId2, sourceDocumentIds.get(2));
        ocrMissionService.save(ocrMission2);
        createdMissionIds.add(missionId2);

        // 批量查找
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).size(), "第一个源文档应该找到1个任务");
        assertTrue(result.get(1).isEmpty(), "不存在的源文档ID应该返回空列表");
        assertEquals(1, result.get(2).size(), "第三个源文档应该找到1个任务");

        // 验证找到的任务有正确的源文档ID
        assertEquals(sourceDocumentIds.get(0), result.get(0).get(0).getSourceDocumentId());
        assertEquals(sourceDocumentIds.get(2), result.get(2).get(0).getSourceDocumentId());
    }

    /**
     * 测试批量查找 - 空列表输入
     */
    @Test
    public void findBatchBySourceDocumentIdEmptyListTest() {
        List<String> emptyList = new ArrayList<>();

        // 批量查找空列表
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(emptyList);

        // 应该返回空列表
        assertNotNull(result);
        assertTrue(result.isEmpty(), "空列表输入应该返回空列表");
    }

    /**
     * 测试批量查找 - 复杂场景
     */
    @Test
    public void findBatchBySourceDocumentIdComplexTest() {
        List<String> sourceDocumentIds = Arrays.asList(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        // 创建复杂的测试数据
        // source-1: 成功的任务
        String successMissionId = UUID.randomUUID().toString();
        OcrMission successMission = OcrMission.Companion.create(successMissionId, sourceDocumentIds.get(0));
        successMission.preparedToExecution();
        successMission.start();
        successMission.success("processed-doc-success");
        ocrMissionService.save(successMission);
        createdMissionIds.add(successMissionId);

        // source-2: 失败的任务
        String failedMissionId = UUID.randomUUID().toString();
        OcrMission failedMission = OcrMission.Companion.create(failedMissionId, sourceDocumentIds.get(1));
        failedMission.preparedToExecution();
        failedMission.start();
        failedMission.failure("Test failure");
        ocrMissionService.save(failedMission);
        createdMissionIds.add(failedMissionId);

        // source-3: 运行中和已创建的任务
        String runningMissionId = UUID.randomUUID().toString();
        OcrMission runningMission = OcrMission.Companion.create(runningMissionId, sourceDocumentIds.get(2));
        runningMission.preparedToExecution();
        runningMission.start();
        ocrMissionService.save(runningMission);
        createdMissionIds.add(runningMissionId);

        String createdMissionId = UUID.randomUUID().toString();
        OcrMission createdMission = OcrMission.Companion.create(createdMissionId, sourceDocumentIds.get(2));
        ocrMissionService.save(createdMission);
        createdMissionIds.add(createdMissionId);

        sleep(); // 确保时间顺序

        // source-4: 没有任务

        // 批量查找
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(4, result.size());

        // 验证source-1: 1个成功任务
        assertEquals(1, result.get(0).size());
        assertTrue(result.get(0).get(0).isSuccess());

        // 验证source-2: 1个失败任务
        assertEquals(1, result.get(1).size());
        assertEquals(MissionStatus.ERROR, result.get(1).get(0).getStatus());

        // 验证source-3: 2个任务，运行中的应该在后面（创建时间更早）
        assertEquals(2, result.get(2).size());
        OcrMission firstInSource3 = result.get(2).get(0);
        OcrMission secondInSource3 = result.get(2).get(1);
        assertEquals(MissionStatus.CREATED, firstInSource3.getStatus());
        assertEquals(MissionStatus.RUNNING, secondInSource3.getStatus());

        // 验证source-4: 没有任务
        assertTrue(result.get(3).isEmpty());
    }

    /**
     * 测试批量查找 - 重复的源文档ID
     */
    @Test
    public void findBatchBySourceDocumentIdWithDuplicatesTest() {
        String sourceDocId = UUID.randomUUID().toString();
        List<String> sourceDocumentIds = Arrays.asList(
                sourceDocId,
                sourceDocId,
                sourceDocId
        );

        // 为源文档创建2个任务
        String missionId1 = UUID.randomUUID().toString();
        OcrMission ocrMission1 = OcrMission.Companion.create(missionId1, sourceDocId);
        ocrMissionService.save(ocrMission1);
        createdMissionIds.add(missionId1);

        sleep();

        String missionId2 = UUID.randomUUID().toString();
        OcrMission ocrMission2 = OcrMission.Companion.create(missionId2, sourceDocId);
        ocrMissionService.save(ocrMission2);
        createdMissionIds.add(missionId2);

        // 批量查找包含重复ID的列表
        List<List<OcrMission>> result = ocrMissionService.findBatchBySourceDocumentId(sourceDocumentIds);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());

        // 每个结果都应该包含相同的任务列表
        for (int i = 0; i < 3; i++) {
            assertEquals(2, result.get(i).size(), "第" + i + "个结果应该包含2个任务");

            List<OcrMission> missions = result.get(i);
            assertEquals(sourceDocId, missions.get(0).getSourceDocumentId());
            assertEquals(sourceDocId, missions.get(1).getSourceDocumentId());
        }
    }

    /**
     * 辅助方法：延迟一小段时间
     */
    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}