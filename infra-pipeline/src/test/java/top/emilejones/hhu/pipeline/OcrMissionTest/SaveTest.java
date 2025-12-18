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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {

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
     * 测试保存单个OCR任务
     */
    @Test
    public void saveTest() {
        // 创建并保存多个OCR任务
        for (int i = 0; i < 10; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            // 使用工厂方法创建OCR任务
            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);

            // 保存任务
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            // 验证任务能够被成功保存
            OcrMission savedMission = ocrMissionService.findById(missionId);
            assertNotNull(savedMission, "保存的任务应该能够被找到");
            assertEquals(missionId, savedMission.getId());
            assertEquals(sourceDocumentId, savedMission.getSourceDocumentId());
            assertEquals(MissionStatus.CREATED, savedMission.getStatus());
        }
    }

    /**
     * 测试更新已存在的OCR任务
     */
    @Test
    public void updateTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        // 创建并保存初始任务
        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证初始状态
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.CREATED, savedMission.getStatus());

        // 更新任务状态为执行中
        savedMission.preparedToExecution();
        savedMission.start();
        ocrMissionService.save(savedMission);

        // 验证更新后的状态
        OcrMission updatedMission = ocrMissionService.findById(missionId);
        assertNotNull(updatedMission);
        assertEquals(MissionStatus.RUNNING, updatedMission.getStatus());
        assertNotNull(updatedMission.getStartTime());

        // 更新任务为成功状态即已Ocr
        String processedDocId = UUID.randomUUID().toString();
        updatedMission.success(processedDocId);
        ocrMissionService.save(updatedMission);

        // 验证最终状态
        OcrMission finalMission = ocrMissionService.findById(missionId);
        assertNotNull(finalMission);
        assertEquals(MissionStatus.SUCCESS, finalMission.getStatus());
        assertTrue(finalMission.isSuccess());
        assertNotNull(finalMission.getEndTime());
    }

    /**
     * 测试任务失败状态的保存
     */
    @Test
    public void saveFailedMissionTest() {
        String missionId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();

        // 创建任务
        OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
        ocrMission.preparedToExecution();
        ocrMission.start();

        // 设置为失败状态
        String errorMessage = "OCR processing failed due to invalid file format";
        ocrMission.failure(errorMessage);

        // 保存失败的任务
        ocrMissionService.save(ocrMission);
        createdMissionIds.add(missionId);

        // 验证失败状态被正确保存
        OcrMission savedMission = ocrMissionService.findById(missionId);
        assertNotNull(savedMission);
        assertEquals(MissionStatus.ERROR, savedMission.getStatus());
        assertTrue(savedMission.isCompleted());
        assertFalse(savedMission.isSuccess());
        assertNotNull(savedMission.getEndTime());

        // 验证失败原因
        OcrMissionResult.Failure failureResult = savedMission.getFailureResult();
        assertEquals(errorMessage, failureResult.getErrorMessage());
    }
}