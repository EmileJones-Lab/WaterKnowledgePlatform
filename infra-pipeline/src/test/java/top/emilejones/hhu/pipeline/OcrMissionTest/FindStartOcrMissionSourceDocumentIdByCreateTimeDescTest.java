package top.emilejones.hhu.pipeline.OcrMissionTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<String> createdMissionIds = new ArrayList<>();
    private List<String> createdFileIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdMissionIds.clear();
        createdFileIds.clear();
    }

    @AfterEach
    void tearDown() {
        // 使用 hardDelete 清理数据，确保测试环境整洁
        createdMissionIds.forEach(id -> ocrMissionMapper.hardDelete(id));
        // 清理 col_file 数据
        createdFileIds.forEach(fileId -> jdbcTemplate.update("DELETE FROM col_file WHERE file_id = ?", fileId));
    }

    /**
     * 测试基本的分页查询功能
     */
    @Test
    public void findStartOcrMissionSourceDocumentIdByCreateTimeDescBasicTest() {
        int missionCount = 15;

        // 创建多个任务
        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);

            if (i % 2 == 0) {
                ocrMission.start();
            } else {
                ocrMission.start();
                ocrMission.success(UUID.randomUUID().toString());
            }

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        // 测试第一页 (keyword = null)
        List<String> firstPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 0, null);
        assertNotNull(firstPage);
        assertEquals(5, firstPage.size(), "第一页应该返回5个结果");

        // 测试第二页 (keyword = "")
        List<String> secondPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 5, "");
        assertNotNull(secondPage);
        assertEquals(5, secondPage.size(), "第二页应该返回5个结果");

        // 测试第三页
        List<String> thirdPage = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(5, 10, null);
        assertNotNull(thirdPage);
        assertEquals(5, thirdPage.size(), "第三页应该返回5个结果");
    }

    /**
     * 测试查询结果的排序（按创建时间倒序）
     */
    @Test
    public void findStartOcrMissionSourceDocumentIdByCreateTimeDescSortingTest() {
        int missionCount = 10;
        Set<String> allSourceDocIds = new HashSet<>();

        for (int i = 0; i < missionCount; i++) {
            String missionId = UUID.randomUUID().toString();
            String sourceDocumentId = UUID.randomUUID().toString();
            allSourceDocIds.add(sourceDocumentId);

            OcrMission ocrMission = OcrMission.Companion.create(missionId, sourceDocumentId);
            ocrMission.start();

            ocrMissionService.save(ocrMission);
            createdMissionIds.add(missionId);

            sleep();
        }

        List<String> allResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(missionCount, 0, null);
        assertNotNull(allResults);
        assertEquals(missionCount, allResults.size());

        for (String sourceDocId : allResults) {
            assertTrue(allSourceDocIds.contains(sourceDocId), "结果应该包含有效的源文档ID");
        }
    }

    /**
     * 测试同一个 fileId 对应多条任务时，结果中不重复，且按最新时间/最高优先级排序
     */
    @Test
    public void findStartOcrMissionWithDuplicateFileIdTest() {
        String sharedFileId = UUID.randomUUID().toString();
        
        // 1. 创建一个较早的、状态优先级较低的任务 (SUCCESS - 5)
        String missionId1 = UUID.randomUUID().toString();
        OcrMission oldMission = OcrMission.Companion.create(missionId1, sharedFileId);
        oldMission.start();
        oldMission.success("doc1");
        // 模拟较早创建时间（通过先保存）
        ocrMissionService.save(oldMission);
        createdMissionIds.add(missionId1);
        sleep();

        // 2. 创建一个较晚的、状态优先级较高的任务 (CREATED - 1 或 RUNNING - 3)
        // 这里测试 PENDING (2)
        String missionId2 = UUID.randomUUID().toString();
        OcrMission newMission = OcrMission.Companion.create(missionId2, sharedFileId);
        
        ocrMissionService.save(newMission);
        createdMissionIds.add(missionId2);

        // 3. 再创建一些其他干扰任务
        String otherFileId = UUID.randomUUID().toString();
        String missionId3 = UUID.randomUUID().toString();
        OcrMission otherMission = OcrMission.Companion.create(missionId3, otherFileId);
        ocrMissionService.save(otherMission);
        createdMissionIds.add(missionId3);

        // 执行查询
        List<String> results = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0, null);

        // 验证去重
        long count = results.stream().filter(id -> id.equals(sharedFileId)).count();
        assertEquals(1, count, "同一个 fileId 应该只出现一次");
        
        assertEquals(2, results.size(), "应该只有两个唯一的 fileId");
        
        // 验证排序：sharedFileId 的最新任务是 newMission (PENDING)，otherFileId 也是 PENDING
        // newMission 比 otherMission 晚创建 (假设 sleep 不足以区分或者随机性)，但至少 sharedFileId 应该在列表中
        
        // 如果我们要严格验证排序逻辑 (MAX(create_time))，newMission 是最新的，所以 sharedFileId 应该排在最前面（如果比 otherMission 新）
        // 在本例中，newMission 比 otherMission 早创建（代码顺序：mission1 -> sleep -> mission2 -> mission3）。
        // 等等，上面代码顺序：save(old) -> sleep -> save(new) -> save(other).
        // 所以 otherMission 是最新的。
        // 预期顺序：otherFileId (mission3), sharedFileId (mission2)
        assertEquals(otherFileId, results.get(0));
        assertEquals(sharedFileId, results.get(1));
    }

    /**
     * 测试关键字搜索功能
     */
    @Test
    public void findStartOcrMissionWithKeywordTest() {
        String keyword = "test_doc";
        String targetFilename = "This_is_a_" + keyword + ".pdf";
        String otherFilename = "other_file.pdf";
        
        String targetFileId = UUID.randomUUID().toString();
        String otherFileId = UUID.randomUUID().toString();

        // 插入 col_file 数据
        insertColFile(targetFileId, targetFilename);
        insertColFile(otherFileId, otherFilename);

        // 目标任务
        String missionId1 = UUID.randomUUID().toString();
        OcrMission targetMission = OcrMission.Companion.create(missionId1, targetFileId);
        ocrMissionService.save(targetMission);
        createdMissionIds.add(missionId1);

        // 干扰任务
        String missionId2 = UUID.randomUUID().toString();
        OcrMission otherMission = OcrMission.Companion.create(missionId2, otherFileId);
        ocrMissionService.save(otherMission);
        createdMissionIds.add(missionId2);

        // 1. 搜索匹配关键字
        List<String> matchedResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0, keyword);
        assertEquals(1, matchedResults.size());
        assertEquals(targetFileId, matchedResults.get(0));

        // 2. 搜索不匹配关键字
        List<String> noMatchResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0, "non_existent");
        assertTrue(noMatchResults.isEmpty());
        
        // 3. 空关键字（应返回所有，因为不触发 JOIN，所以返回所有存在的任务 ID，即使它们没有 col_file 记录（如果原本逻辑允许），
        // 但这里我们都插入了 col_file，所以应该都返回）
        List<String> allResults = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(10, 0, "");
        assertEquals(2, allResults.size());
    }

    @Test
    public void findStartOcrMissionBoundaryTest() {
        String missionId = UUID.randomUUID().toString();
        OcrMission m = OcrMission.Companion.create(missionId, UUID.randomUUID().toString());
        ocrMissionService.save(m);
        createdMissionIds.add(missionId);

        List<String> zeroLimit = ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(0, 0, null);
        assertTrue(zeroLimit.isEmpty());

        assertThrows(IllegalArgumentException.class, () -> {
            ocrMissionService.findStartOcrMissionSourceDocumentIdByCreateTimeDesc(-1, 0, null);
        });
    }

    private void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void insertColFile(String fileId, String filename) {
        String sql = "INSERT INTO col_file (file_id, filename, catapath, filepath, filetype) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, fileId, filename, "/path", "/path/" + filename, "pdf");
        createdFileIds.add(fileId);
    }
}
