package top.emilejones.hhu.pipeline.OcrMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.MissionStatus;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission;
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.OcrMissionMapper;
import top.emilejones.hhu.pipeline.services.OcrMissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务根据ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByIdTest {

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
     * 测试查找已存在的OCR任务
     */
    @Test
    public void findByIdExistingTest() {
        // 创建并保存任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 查找任务
        OcrMission foundMission = ocrMissionService.findById(missionId);

        // 验证找到的任务
        assertNotNull(foundMission, "应该能够找到已保存的任务");
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.CREATED, foundMission.getStatus());
    }

    /**
     * 测试查找不存在的OCR任务
     */
    @Test
    public void findByIdNonExistingTest() {
        // 使用不存在的ID查找
        String nonExistingId = UUID.randomUUID().toString();
        OcrMission foundMission = ocrMissionService.findById(nonExistingId);

        // 应该返回null
        assertNull(foundMission, "不存在的任务ID应该返回null");
    }

    /**
     * 测试查找已完成任务的完整信息
     */
    @Test
    public void findByIdCompletedMissionTest() {
        // 创建并完成任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String processedDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();
        ocrMission.success(processedDocumentId);

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 查找任务
        OcrMission foundMission = ocrMissionService.findById(missionId);

        // 验证完整信息
        assertNotNull(foundMission);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.SUCCESS, foundMission.getStatus());
        assertTrue(foundMission.isSuccess());
        assertTrue(foundMission.isCompleted());
        assertNotNull(foundMission.getStartTime());
        assertNotNull(foundMission.getEndTime());

        // 验证成功结果
        OcrMissionResult.Success successResult = foundMission.getSuccessResult();
        assertEquals(processedDocumentId, successResult.getMarkdownDocumentId());
    }

    /**
     * 测试查找失败任务的完整信息
     */
    @Test
    public void findByIdFailedMissionTest() {
        // 创建并失败的任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String errorMessage = "Test error message: file format not supported";

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();
        ocrMission.failure(errorMessage);

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 查找任务
        OcrMission foundMission = ocrMissionService.findById(missionId);

        // 验证失败信息
        assertNotNull(foundMission);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.ERROR, foundMission.getStatus());
        assertFalse(foundMission.isSuccess());
        assertTrue(foundMission.isCompleted());
        assertNotNull(foundMission.getStartTime());
        assertNotNull(foundMission.getEndTime());

        // 验证失败原因
        OcrMissionResult.Failure failureResult = foundMission.getFailureResult();
        assertEquals(errorMessage, failureResult.getErrorMessage());
    }

    /**
     * 测试查找运行中任务的信息
     */
    @Test
    public void findByIdRunningMissionTest() {
        // 创建运行中的任务
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();

        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 查找任务
        OcrMission foundMission = ocrMissionService.findById(missionId);

        // 验证运行中状态
        assertNotNull(foundMission);
        assertEquals(missionId, foundMission.getId());
        assertEquals(sourceDocumentId, foundMission.getSourceDocumentId());
        assertEquals(MissionStatus.RUNNING, foundMission.getStatus());
        assertFalse(foundMission.isCompleted());
        assertNotNull(foundMission.getStartTime());
        assertNull(foundMission.getEndTime());
    }

    /**
     * 测试查找不同状态的任务
     */
    @Test
    public void findByIdAllStatusesTest() {
        String[] missionIds = new String[5];
        MissionStatus[] expectedStatuses = {
                MissionStatus.CREATED,
                MissionStatus.PENDING,
                MissionStatus.RUNNING,
                MissionStatus.SUCCESS,
                MissionStatus.ERROR
        };

        // 创建不同状态的任务
        for (int i = 0; i < 5; i++) {
            missionIds[i] = UUID.randomUUID().toString();
            OcrMission ocrMission = OcrMission.Companion.create(missionIds[i], UUID.randomUUID().toString());

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
                    ocrMission.success(UUID.randomUUID().toString());
                    break;
                case 4:
                    ocrMission.preparedToExecution();
                    ocrMission.start();
                    ocrMission.failure("Error for test " + i);
                    break;
            }

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionIds[i]);
        }

        // 验证所有状态的任务都能正确查找
        for (int i = 0; i < 5; i++) {
            OcrMission foundMission = ocrMissionService.findById(missionIds[i]);
            assertNotNull(foundMission, "任务 " + i + " 应该能够被找到");
            assertEquals(expectedStatuses[i], foundMission.getStatus(),
                    "任务 " + i + " 状态应该匹配");
        }
    }
}