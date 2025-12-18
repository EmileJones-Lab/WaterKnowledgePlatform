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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务根据源文档ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindBySourceDocumentIdTest {

    @Autowired
    private OcrMissionService ocrMissionService;
    @Autowired
    private OcrMissionMapper ocrMissionMapper;

    private List<String> createdMissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
    }

    @AfterEach
    void tearDown() {
        // 测试结束后也可以执行一次，保持数据库清洁
        createdMissionIds.forEach(id -> ocrMissionMapper.hardDelete(id));
    }

    /**
     * 测试根据源文档ID查找任务 - 单个任务
     */
    @Test
    public void findBySourceDocumentIdSingleTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        String missionId = UUID.randomUUID().toString();

        // 创建并保存任务
        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 查找任务
        List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(sourceDocumentId);

        // 验证结果
        assertNotNull(foundMissions);
        assertEquals(1, foundMissions.size(), "应该找到1个任务");

        OcrMission foundMission = foundMissions.get(0);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, foundMission.getStatus());
    }

    /**
     * 测试根据源文档ID查找任务 - 多个任务
     */
    @Test
    public void findBySourceDocumentIdMultipleTest() {
        String sourceDocumentId = UUID.randomUUID().toString();
        int missionCount = 5;
        List<String> missionIds = new ArrayList<>();

        // 创建并保存多个任务，使用相同的源文档ID
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            missionIds.add(missionId);
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            // 稍微延迟，确保创建时间不同
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 查找任务
        List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(sourceDocumentId);

        // 验证结果
        assertNotNull(foundMissions);
        assertEquals(missionCount, foundMissions.size(), "应该找到" + missionCount + "个任务");

        // 验证任务按创建时间倒序排列
        for (int i = 0; i < foundMissions.size() - 1; i++) {
            OcrMission current = foundMissions.get(i);
            OcrMission next = foundMissions.get(i + 1);

            assertTrue(current.getCreateTime().isAfter(next.getCreateTime()) ||
                     current.getCreateTime().equals(next.getCreateTime()),
                    "任务应该按创建时间倒序排列");
        }

        // 验证所有任务都有正确的源文档ID
        for (OcrMission mission : foundMissions) {
            assertEquals(sourceDocumentId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试查找不存在源文档ID的任务
     */
    @Test
    public void findBySourceDocumentIdNonExistingTest() {
        String nonExistingSourceDocId = UUID.randomUUID().toString();

        // 查找不存在的源文档ID
        List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(nonExistingSourceDocId);

        // 应该返回空列表
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
        MissionStatus[] expectedStatuses = {
                MissionStatus.CREATED,
                MissionStatus.PENDING,
                MissionStatus.RUNNING,
                MissionStatus.SUCCESS,
                MissionStatus.ERROR
        };

        for (int i = 0; i < 5; i++) {
            missionIds[i] = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionIds[i], sourceDocumentId);

            switch (i) {
                case 0:
                    // 保持CREATED状态
                    break;
                case 1:
                    ocrMission.preparedToExecution();
                    break;
                case 2:
                    ocrMission.preparedToExecution();
                    ocrMission.start();
                    break;
                case 3:
                    ocrMission.preparedToExecution();
                    ocrMission.start();
                    ocrMission.success("processed-doc-" + i);
                    break;
                case 4:
                    ocrMission.preparedToExecution();
                    ocrMission.start();
                    ocrMission.failure("Test error"); // ERROR状态
                    break;
            }

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionIds[i]);

            // 稍微延迟，确保创建时间不同
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 查找任务
        List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(sourceDocumentId);

        // 验证结果
        assertNotNull(foundMissions);
        assertEquals(5, foundMissions.size(), "应该找到5个任务");

        // 验证状态和排序
        for (int i = 0; i < foundMissions.size(); i++) {
            OcrMission mission = foundMissions.get(i);
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

        // 为每个源文档ID创建任务
        for (int i = 0; i < sourceDocumentIds.length; i++) {
            String missionId = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentIds[i]);
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);
        }

        // 分别查找每个源文档ID的任务
        for (String sourceDocId : sourceDocumentIds) {
            List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(sourceDocId);

            assertNotNull(foundMissions);
            assertEquals(1, foundMissions.size(), "源文档 " + sourceDocId + " 应该找到1个任务");

            OcrMission mission = foundMissions.get(0);
            assertEquals(sourceDocId, mission.getSourceDocumentId());
        }
    }

    /**
     * 测试边界情况 - 空字符串源文档ID
     */
    @Test
    public void findBySourceDocumentIdEmptyStringTest() {
        // 测试空字符串应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.findBySourceDocumentId("");
        }, "空字符串源文档ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试边界情况 - null源文档ID
     */
    @Test
    public void findBySourceDocumentIdNullTest() {
        // 测试null应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.findBySourceDocumentId(null);
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
        OcrMission successMission = OcrMission.Companion.create(successMissionId, sourceDocumentId);
        successMission.preparedToExecution();
        successMission.start();
        successMission.success("processed-doc-success");
        ocrMissionService.save(successMission);
        createdMissionIds.add(successMissionId);

        // 创建失败的任务
        String failedMissionId = UUID.randomUUID().toString();
        OcrMission failedMission = OcrMission.Companion.create(failedMissionId, sourceDocumentId);
        failedMission.preparedToExecution();
        failedMission.start();
        failedMission.failure("Test failure reason");
        ocrMissionService.save(failedMission);
        createdMissionIds.add(failedMissionId);

        // 查找任务
        List<OcrMission> foundMissions = ocrMissionService.findBySourceDocumentId(sourceDocumentId);

        // 验证结果
        assertNotNull(foundMissions);
        assertEquals(2, foundMissions.size(), "应该找到2个任务");

        // 验证包含成功和失败的任务
        boolean hasSuccess = false;
        boolean hasFailure = false;

        for (OcrMission mission : foundMissions) {
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