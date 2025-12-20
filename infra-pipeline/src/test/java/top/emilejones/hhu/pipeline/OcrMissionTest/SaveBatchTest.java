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
 * OCR任务批量保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveBatchTest {

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
     * 测试批量保存多个OCR任务
     */
    @Test
    public void saveBatchTest() {
        List<OcrMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        // 创建多个OCR任务
        for (int i = 0; i < 20; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            missionList.add(ocrMission);
            missionIds.add(missionId);
        }

        // 批量保存任务
        ocrMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        // 验证所有任务都被成功保存
        for (String missionId : missionIds) {
            OcrMission savedMission = ocrMissionService.findById(missionId);
            assertNotNull(savedMission, "任务ID: " + missionId + " 应该能够被找到");
            assertEquals(missionId, savedMission.getId());
            assertEquals(MissionStatus.CREATED, savedMission.getStatus());
        }
    }

    /**
     * 测试批量保存空列表
     */
    @Test
    public void saveEmptyBatchTest() {
        // 保存空列表应该不会抛出异常
        assertDoesNotThrow(() -> {
            ocrMissionService.saveBatch(new ArrayList<>());
        });
    }

    /**
     * 测试批量保存不同状态的任务
     */
    @Test
    public void saveMixedStatusBatchTest() {
        List<OcrMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        // 创建不同状态的任务
        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);

            // 设置不同的状态
            switch (i % 4) {
                case 0:
                    // 保持CREATED状态
                    break;
                case 1:
                    break;
                case 2:
                    ocrMission.start();
                    break;
                case 3:
                    ocrMission.start();
                    ocrMission.success(UUID.randomUUID().toString());
                    break;
            }

            missionList.add(ocrMission);
            missionIds.add(missionId);
        }

        // 批量保存任务
        ocrMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        // 验证所有任务都被正确保存，且状态正确
        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);
            OcrMission savedMission = ocrMissionService.findById(missionId);
            assertNotNull(savedMission, "任务ID: " + missionId + " 应该能够被找到");

            // 验证状态
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
        List<OcrMission> missionList = new ArrayList<>();
        List<String> missionIds = new ArrayList<>();

        // 第一阶段：创建并保存任务
        for (int i = 0; i < 5; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            missionList.add(ocrMission);
            missionIds.add(missionId);
        }

        ocrMissionService.saveBatch(missionList);
        createdMissionIds.addAll(missionIds);

        // 第二阶段：更新任务状态
        List<OcrMission> updatedMissionList = new ArrayList<>();
        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);

            // 重新获取任务并更新状态
            OcrMission existingMission = ocrMissionService.findById(missionId);
            assertNotNull(existingMission);

            existingMission.start();

            if (i % 2 == 0) {
                // 一半成功
                String processedDocId = UUID.randomUUID().toString();
                existingMission.success(processedDocId);
            } else {
                // 一半失败
                existingMission.failure("Processing failed for test");
            }

            updatedMissionList.add(existingMission);
        }

        // 批量更新任务
        ocrMissionService.saveBatch(updatedMissionList);

        // 验证更新结果
        for (int i = 0; i < missionIds.size(); i++) {
            String missionId = missionIds.get(i);
            OcrMission finalMission = ocrMissionService.findById(missionId);
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