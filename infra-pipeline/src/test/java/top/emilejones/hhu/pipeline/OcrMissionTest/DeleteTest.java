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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务删除测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class DeleteTest {

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
     * 测试删除已存在的OCR任务
     */
    @Test
    public void deleteExistingMissionTest() {
        // 创建并保存任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证任务存在
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission, "任务应该存在");

        // 删除任务
        ocrMissionService.delete(missionId);

        // 验证任务已被删除
        OcrMission deletedMission = ocrMissionService.findById(missionId);
        assertNull(deletedMission, "删除后任务应该不存在");
    }

    /**
     * 测试删除不存在的OCR任务（幂等性测试）
     */
    @Test
    public void deleteNonExistingMissionTest() {
        // 使用不存在的任务ID删除，应该不会抛出异常
        String nonExistingMissionId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> {
            ocrMissionService.delete(nonExistingMissionId);
        }, "删除不存在的任务不应该抛出异常");

        // 验证确实不存在
        OcrMission mission = ocrMissionService.findById(nonExistingMissionId);
        assertNull(mission, "不存在的任务ID应该返回null");
    }

    /**
     * 测试删除已完成的任务
     */
    @Test
    public void deleteCompletedMissionTest() {
        // 创建并完成任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();
        ocrMission.success(UUID.randomUUID().toString());

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且已完成
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertTrue(savedMission.isCompleted());
        assertTrue(savedMission.isSuccess());

        // 删除任务
        ocrMissionService.delete(missionId);

        // 验证任务已被删除
        OcrMission deletedMission = ocrMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试删除失败的任务
     */
    @Test
    public void deleteFailedMissionTest() {
        // 创建并失败的任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();
        ocrMission.failure("Test failure for deletion");

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且已失败
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertTrue(savedMission.isCompleted());
        assertFalse(savedMission.isSuccess());
        assertEquals(MissionStatus.ERROR, savedMission.getStatus());

        // 删除任务
        ocrMissionService.delete(missionId);

        // 验证任务已被删除
        OcrMission deletedMission = ocrMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试删除运行中的任务
     */
    @Test
    public void deleteRunningMissionTest() {
        // 创建运行中的任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证任务存在且正在运行
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.RUNNING, savedMission.getStatus());
        assertNotNull(savedMission.getStartTime());
        assertNull(savedMission.getEndTime());

        // 删除任务
        ocrMissionService.delete(missionId);

        // 验证任务已被删除
        OcrMission deletedMission = ocrMissionService.findById(missionId);
        assertNull(deletedMission);
    }

    /**
     * 测试重复删除同一任务（幂等性测试）
     */
    @Test
    public void deleteMissionMultipleTimesTest() {
        // 创建并保存任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证任务存在
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);

        // 第一次删除
        assertDoesNotThrow(() -> {
            ocrMissionService.delete(missionId);
        });

        // 验证任务已被删除
        OcrMission deletedOnce = ocrMissionService.findById(missionId);
        assertNull(deletedOnce);

        // 第二次删除，应该不抛出异常
        assertDoesNotThrow(() -> {
            ocrMissionService.delete(missionId);
        });

        // 第三次删除，应该不抛出异常
        assertDoesNotThrow(() -> {
            ocrMissionService.delete(missionId);
        });

        // 验证任务仍然不存在
        OcrMission finallyDeleted = ocrMissionService.findById(missionId);
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

        OcrMission ocrMission1 = OcrMission.Companion.create(missionId1, sourceDocumentId);
        OcrMission ocrMission2 = OcrMission.Companion.create(missionId2, sourceDocumentId);
        OcrMission ocrMission3 = OcrMission.Companion.create(missionId3, sourceDocumentId);

        ocrMission1.preparedToExecution();
        ocrMission1.start();
        ocrMission1.success(UUID.randomUUID().toString());

        ocrMissionService.save(ocrMission1);
        ocrMissionService.save(ocrMission2);
        ocrMissionService.save(ocrMission3);

        createdMissionIds.add(missionId1);
        createdMissionIds.add(missionId2);
        createdMissionIds.add(missionId3);

        // 验证找到3个任务
        var missionsBySource = ocrMissionService.findBySourceDocumentId(sourceDocumentId);
        assertEquals(3, missionsBySource.size());

        // 删除中间的任务
        ocrMissionService.delete(missionId2);

        // 验证删除后的状态
        OcrMission deletedMission = ocrMissionService.findById(missionId2);
        assertNull(deletedMission);

        OcrMission remainingMission1 = ocrMissionService.findById(missionId1);
        assertNotNull(remainingMission1);

        OcrMission remainingMission3 = ocrMissionService.findById(missionId3);
        assertNotNull(remainingMission3);

        // 验证按源文档查询现在只返回2个任务
        var remainingMissionsBySource = ocrMissionService.findBySourceDocumentId(sourceDocumentId);
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
        // 测试空字符串应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.delete("");
        }, "空字符串任务ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试边界情况 - null任务ID
     */
    @Test
    public void deleteNullIdTest() {
        // 测试null应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.delete(null);
        }, "null任务ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试删除任务对分页查询的影响
     */
    @Test
    public void deleteMissionAndPaginationTest() {
        int missionCount = 10;
        List<String> sourceDocumentIds = new ArrayList<>();

        // 创建多个已启动的任务
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            sourceDocumentIds.add(sourceDocumentId);

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution();
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);
        }

        // 验证初始数量
        var initialResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(missionCount, 0);
        assertEquals(missionCount, initialResults.size());

        // 删除几个任务
        for (int i = 0; i < 3; i++) {
            String sourceDocumentId = sourceDocumentIds.get(i);
            var missions = ocrMissionService.findBySourceDocumentId(sourceDocumentId);
            if (!missions.isEmpty()) {
                String missionToDelete = missions.get(0).getId();
                ocrMissionService.delete(missionToDelete);
            }
        }

        // 验证删除后的数量
        var afterDeleteResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(missionCount, 0);
        assertEquals(missionCount - 3, afterDeleteResults.size());
    }
}