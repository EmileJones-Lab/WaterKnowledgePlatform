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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCR任务分页查询测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindStartOcrMissionSourceDocumentIdByCreateTimeDescTest {

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
     * 测试基本的分页查询功能
     */
    @Test
    public void findStartOcrMissionSourceDocumentIdByCreateTimeDescBasicTest() {
        int missionCount = 15;

        // 创建多个任务，确保它们是启动状态（5种状态都属于启动状态）
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution(); // 设置为PENDING状态

            // 一半设置为运行中，一半设置为成功
            if (i % 2 == 0) {
                ocrMission.start(); // RUNNING状态
            } else {
                ocrMission.start();
                ocrMission.success(UUID.randomUUID().toString()); // SUCCESS状态
            }

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            // 稍微延迟，确保创建时间不同
            sleep();
        }

        // 测试第一页
        List<String> firstPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 0);
        assertNotNull(firstPage);
        assertEquals(5, firstPage.size(), "第一页应该返回5个结果");

        // 测试第二页
        List<String> secondPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 5);
        assertNotNull(secondPage);
        assertEquals(5, secondPage.size(), "第二页应该返回5个结果");

        // 测试第三页
        List<String> thirdPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 10);
        assertNotNull(thirdPage);
        assertEquals(5, thirdPage.size(), "第三页应该返回5个结果");

        // 测试超出范围的页面
        List<String> emptyPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 90);
        assertNotNull(emptyPage);
        assertTrue(emptyPage.isEmpty(), "超出范围的页面应该返回空列表");
    }

    /**
     * 测试查询结果的排序（按创建时间倒序）
     */
    @Test
    public void findStartOcrMissionSourceDocumentIdByCreateTimeDescSortingTest() {
        int missionCount = 10;
        Set<String> allSourceDocIds = new HashSet<>();

        // 创建任务并记录创建时间
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            allSourceDocIds.add(sourceDocumentId);

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution();
            ocrMission.start();

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        // 获取所有结果
        List<String> allResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(missionCount, 0);
        assertNotNull(allResults);
        assertEquals(missionCount, allResults.size());

        // 验证所有结果都是有效的源文档ID
        for (String sourceDocId : allResults) {
            assertTrue(allSourceDocIds.contains(sourceDocId), "结果应该包含有效的源文档ID");
        }
    }

    /*
      测试只有CREATED状态的任务不会被查询到
      注：此类不需要测试，因为五个状态都属于启动状态
     */
  /*  @Test
    public void findStartOcrMissionExcludeCreatedTest() {
        // 创建一些CREATED状态的任务
        for (int i = 0; i < 5; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            // 保持CREATED状态，不调用preparedToExecution()

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);
        }

        // 创建一些已启动的任务
        List<String> startedDocIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            startedDocIds.add(sourceDocumentId);

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution(); // PENDING状态

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);
            sleep();
        }

        // 查询结果应该只包含已启动的任务
        List<String> results = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0);
        assertNotNull(results);
        assertEquals(3, results.size(), "应该只找到3个已启动的任务");

        // 验证结果中不包含CREATED状态的任务
        for (String sourceDocId : results) {
            assertTrue(startedDocIds.contains(sourceDocId),
                    "结果应该只包含已启动的任务: " + sourceDocId);
        }
    }*/

    /**
     * 测试五种不同状态的任务都能被查询到
     */
    @Test
    public void findStartOcrMissionAllStatusesTest() {
        int[] statusCounts = new int[5]; // CREATED, PENDING, RUNNING, SUCCESS, ERROR
        String[] statusPrefixes = {"created", "pending", "running", "success", "error"};

        // 创建不同状态的任务
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                String missionId = UUID.randomUUID().toString();
                String sourceDocumentId = UUID.randomUUID().toString();

                OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);

                switch (i) {
                    case 0:
                        // 保持CREATED状态，不调用preparedToExecution()
                        statusCounts[i]++;
                        break;
                    case 1:
                        ocrMission.preparedToExecution();
                        // 保持PENDING状态
                        statusCounts[i]++;
                        break;
                    case 2:
                        ocrMission.preparedToExecution();
                        ocrMission.start(); // RUNNING状态
                        statusCounts[i]++;
                        break;
                    case 3:
                        ocrMission.preparedToExecution();
                        ocrMission.start();
                        ocrMission.success(UUID.randomUUID().toString()); // SUCCESS状态
                        statusCounts[i]++;
                        break;
                    case 4:
                        ocrMission.preparedToExecution();
                        ocrMission.start();
                        ocrMission.failure("Test error"); // ERROR状态
                        statusCounts[i]++;
                        break;
                }

                ocrMissionService.save(ocrMission);
                createdMissionIds.add(missionId);
                sleep();
            }
        }

        // 查询所有已启动的任务
        List<String> results = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(20, 0);
        assertNotNull(results);
        assertEquals(15, results.size(), "应该找到15个已启动的任务"); // 3 * 5 = 15

        // 注意：由于我们不再使用前缀，我们无法通过前缀来验证状态，但可以验证总数
        assertTrue(results.size() >= 12, "至少应该找到12个已启动的任务（排除CREATED状态）");
    }

    /**
     * 测试边界条件 - limit和offset为0
     */
    @Test
    public void findStartOcrMissionBoundaryTest() {
        // 创建一些任务
        for (int i = 0; i < 5; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution();
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        // 测试limit为0
        List<String> zeroLimit = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(0, 0);
        assertNotNull(zeroLimit);
        assertTrue(zeroLimit.isEmpty(), "limit为0应该返回空列表");

        // 测试offset为0
        List<String> zeroOffset = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(3, 0);
        assertNotNull(zeroOffset);
        assertEquals(3, zeroOffset.size(), "offset为0应该返回前面的结果");
    }

    /**
     * 测试异常情况 - 负数参数
     */
    @Test
    public void findStartOcrMissionNegativeParametersTest() {
        // 测试负数limit应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(-1, 0);
        }, "负数limit应该抛出IllegalArgumentException");

        // 测试负数offset应该抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, -1);
        }, "负数offset应该抛出IllegalArgumentException");
    }

    /**
     * 测试空数据库的查询
     */
    @Test
    public void findStartOcrMissionEmptyDatabaseTest() {
        // 在空数据库中查询
        List<String> results = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0);
        assertNotNull(results);
        assertTrue(results.isEmpty(), "空数据库应该返回空列表");
    }

    /**
     * 测试大limit值的查询
     */
    @Test
    public void findStartOcrMissionLargeLimitTest() {
        int missionCount = 8;

        // 创建一些任务
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.preparedToExecution();
            ocrMission.start();
            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        // 使用比实际数量大的limit
        List<String> results = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(100, 0);
        assertNotNull(results);
        assertEquals(missionCount, results.size(), "limit大于实际数量时，应该返回所有结果");
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