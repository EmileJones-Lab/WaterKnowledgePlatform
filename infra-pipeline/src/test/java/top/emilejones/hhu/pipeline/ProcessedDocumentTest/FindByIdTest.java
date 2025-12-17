package top.emilejones.hhu.pipeline.ProcessedDocumentTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 处理后文档根据ID查找测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class FindByIdTest {

    @Autowired
    private ProcessedDocumentService processedDocumentService;

    private List<String> createdDocumentIds = new ArrayList<>();
    private List<String> createdFilePaths = new ArrayList<>();
    private String testBaseDir = "test-docs-findbyid";

    @BeforeEach
    void setUp() {
        createdDocumentIds.clear();
        createdFilePaths.clear();
        // 创建测试目录
        try {
            Files.createDirectories(Paths.get(testBaseDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test directory", e);
        }
    }

    @AfterEach
    void tearDown() {
        // 清理测试创建的所有文档元数据
        createdDocumentIds.clear();

        // 清理测试创建的所有文件
        for (String filePath : createdFilePaths) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException e) {
                // 忽略删除异常
            }
        }
        createdFilePaths.clear();

        // 清理测试目录
        try {
            Files.deleteIfExists(Paths.get(testBaseDir));
        } catch (IOException e) {
            // 忽略删除异常
        }
    }

    /**
     * 测试查找已存在的Markdown文档
     */
    @Test
    public void findByIdExistingMarkdownDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-markdown.md";
        String filePath = testBaseDir + "/" + fileName;
        String content = "# Test Markdown Document\n\nThis is a test markdown file.";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 查找文档
        ProcessedDocument foundDocument = processedDocumentService.findById(documentId)
                .orElse(null);

        // 验证找到的文档
        assertNotNull(foundDocument, "应该能够找到已保存的文档");
        assertEquals(documentId, foundDocument.getId());
        assertEquals(sourceDocumentId, foundDocument.getSourceDocumentId());
        assertEquals(filePath, foundDocument.getFilePath());
        assertEquals(ProcessedDocumentType.MARKDOWN, foundDocument.getProcessedDocumentType());
        assertNotNull(foundDocument.getCreateTime());
    }

    /**
     * 测试查找已存在的图片文档
     */
    @Test
    public void findByIdExistingImageDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-image.png";
        String filePath = testBaseDir + "/" + fileName;
        byte[] imageContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        // 创建并保存图片文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.PNG
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageContent);
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 查找文档
        ProcessedDocument foundDocument = processedDocumentService.findById(documentId)
                .orElse(null);

        // 验证找到的文档
        assertNotNull(foundDocument, "应该能够找到已保存的图片文档");
        assertEquals(documentId, foundDocument.getId());
        assertEquals(sourceDocumentId, foundDocument.getSourceDocumentId());
        assertEquals(filePath, foundDocument.getFilePath());
        assertEquals(ProcessedDocumentType.PNG, foundDocument.getProcessedDocumentType());
        assertNotNull(foundDocument.getCreateTime());
    }

    /**
     * 测试查找不存在的文档
     */
    @Test
    public void findByIdNonExistingDocumentTest() {
        // 使用不存在的ID查找
        String nonExistingId = UUID.randomUUID().toString();

        // 查找文档
        ProcessedDocument foundDocument = processedDocumentService.findById(nonExistingId)
                .orElse(null);

        // 应该返回Optional.empty()
        assertNull(foundDocument, "不存在的文档ID应该返回Optional.empty()");
    }

    /**
     * 测试查找空ID的文档
     */
    @Test
    public void findByIdEmptyIdTest() {
        // 测试空字符串ID
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.findById("");
        }, "空字符串ID应该抛出IllegalArgumentException");

        // 测试null ID
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.findById(null);
        }, "null ID应该抛出IllegalArgumentException");

        // 测试空白字符串ID
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.findById("   ");
        }, "空白字符串ID应该抛出IllegalArgumentException");
    }

    /**
     * 测试查找不同创建时间的文档
     */
    @Test
    public void findByIdDifferentCreateTimeTest() throws InterruptedException {
        String[] documentIds = new String[3];
        String[] sourceDocumentIds = new String[3];
        String[] fileNames = new String[]{"doc1.md", "doc2.md", "doc3.md"};
        String[] filePaths = new String[3];
        Instant[] createTimes = new Instant[3];

        // 创建不同时间的文档
        for (int i = 0; i < 3; i++) {
            documentIds[i] = UUID.randomUUID().toString();
            sourceDocumentIds[i] = UUID.randomUUID().toString();
            filePaths[i] = testBaseDir + "/" + fileNames[i];

            // 间隔一段时间创建文档，确保创建时间不同
            if (i > 0) {
                Thread.sleep(10); // 等待10毫秒
            }

            createTimes[i] = Instant.now();

            ProcessedDocument processedDocument = new ProcessedDocument(
                    documentIds[i],
                    sourceDocumentIds[i],
                    filePaths[i],
                    createTimes[i],
                    ProcessedDocumentType.MARKDOWN
            );

            String content = "# Document " + (i + 1) + "\n\nContent for document " + (i + 1);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            processedDocumentService.save(processedDocument, inputStream);
            createdDocumentIds.add(documentIds[i]);
            createdFilePaths.add(filePaths[i]);
        }

        // 验证每个文档都能正确查找且创建时间正确
        for (int i = 0; i < 3; i++) {
            ProcessedDocument foundDocument = processedDocumentService.findById(documentIds[i])
                    .orElse(null);
            assertNotNull(foundDocument, "文档 " + (i + 1) + " 应该能够被找到");
            assertEquals(documentIds[i], foundDocument.getId());
            assertEquals(sourceDocumentIds[i], foundDocument.getSourceDocumentId());
            assertEquals(filePaths[i], foundDocument.getFilePath());
            assertEquals(ProcessedDocumentType.MARKDOWN, foundDocument.getProcessedDocumentType());

            // 验证创建时间（允许毫秒级误差）
            assertNotNull(foundDocument.getCreateTime());
            long timeDifference = Math.abs(foundDocument.getCreateTime().toEpochMilli() - createTimes[i].toEpochMilli());
            assertTrue(timeDifference < 1000, "创建时间差异应该小于1秒");
        }
    }

    /**
     * 测试查找多个文档的完整性
     */
    @Test
    public void findByIdMultipleDocumentsIntegrityTest() {
        int documentCount = 5;
        String[] documentIds = new String[documentCount];
        String[] sourceDocumentIds = new String[documentCount];

        // 创建多个文档
        for (int i = 0; i < documentCount; i++) {
            documentIds[i] = UUID.randomUUID().toString();
            sourceDocumentIds[i] = UUID.randomUUID().toString();
            String filePath = testBaseDir + "/doc" + i + ".md";

            ProcessedDocument processedDocument = new ProcessedDocument(
                    documentIds[i],
                    sourceDocumentIds[i],
                    filePath,
                    Instant.now(),
                    ProcessedDocumentType.MARKDOWN
            );

            String content = "# Document " + i + "\n\nThis is document number " + i;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            processedDocumentService.save(processedDocument, inputStream);
            createdDocumentIds.add(documentIds[i]);
            createdFilePaths.add(filePath);
        }

        // 验证所有文档都能正确查找
        for (int i = 0; i < documentCount; i++) {
            ProcessedDocument foundDocument = processedDocumentService.findById(documentIds[i])
                    .orElse(null);
            assertNotNull(foundDocument, "文档 " + i + " 应该能够被找到");
            assertEquals(documentIds[i], foundDocument.getId());
            assertEquals(sourceDocumentIds[i], foundDocument.getSourceDocumentId());
            assertEquals(ProcessedDocumentType.MARKDOWN, foundDocument.getProcessedDocumentType());
        }

        // 验证交叉查找（确保不会串数据）
        for (int i = 0; i < documentCount; i++) {
            int differentIndex = (i + 1) % documentCount;
            ProcessedDocument foundDocument = processedDocumentService.findById(documentIds[i])
                    .orElse(null);
            assertNotNull(foundDocument);
            assertEquals(documentIds[i], foundDocument.getId(), "ID " + i + " 应该返回对应的文档");
            assertNotEquals(documentIds[differentIndex], foundDocument.getId(), "ID " + i + " 不应该返回其他文档");
        }
    }

    /**
     * 测试查找包含特殊字符的文档ID
     * 修改 ID 长度，使其符合数据库 char(36) 约束。
     */
    @Test
    public void findByIdSpecialCharactersTest() {
        // 创建包含各种特殊字符的文档ID
        String baseId = UUID.randomUUID().toString().substring(0, 25);

        // 确保总长度不超过 36:
        String[] specialIds = {
                baseId + "-dash",       // 25 + 5 = 30
                baseId + "_under",      // 25 + 6 = 31
                baseId + ".dot.id",     // 25 + 6 = 31
                baseId + "0123456789-" // 25 + 11 = 36 (最大长度测试)
        };

        for (String documentId : specialIds) {
            String sourceDocumentId = UUID.randomUUID().toString();
            // 由于 filePath 的长度可能也有数据库限制，这里简化一下
            String filePath = testBaseDir + "/special-" + documentId.replaceAll("[^a-zA-Z0-9]", "") + ".md";

            ProcessedDocument processedDocument = new ProcessedDocument(
                    documentId,
                    sourceDocumentId,
                    filePath,
                    Instant.now(),
                    ProcessedDocumentType.MARKDOWN
            );

            String content = "# Special ID Document\n\nDocument ID: " + documentId;
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());

            // 1. 保存操作 (现在不会因为 ID 过长而报错)
            processedDocumentService.save(processedDocument, inputStream);
            createdDocumentIds.add(documentId);
            createdFilePaths.add(filePath);

            // 2. 查找文档
            ProcessedDocument foundDocument = processedDocumentService.findById(documentId)
                    .orElse(null);

            // 3. 断言
            assertNotNull(foundDocument, "包含特殊字符的文档ID应该能够被找到: " + documentId);
            assertEquals(documentId, foundDocument.getId());
        }
    }

    /**
     * 测试查找超长文档ID的情况
     */
    @Test
    public void findByIdLongDocumentIdTest() {
        // 1. 创建一个很长的文档ID (长度约 137，远超 char(36) 约束)
        StringBuilder longIdBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longIdBuilder.append("a");
        }
        String longDocumentId = longIdBuilder.toString() + "-" + UUID.randomUUID().toString();

        String sourceDocumentId = UUID.randomUUID().toString();
        String filePath = testBaseDir + "/long-id-doc.md";

        ProcessedDocument processedDocument = new ProcessedDocument(
                longDocumentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        String content = "# Long ID Document\n\nThis document has a very long ID.";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());

        // 2. 核心修改：断言保存操作会抛出 DataAccessException
        // 这代表了底层的 MysqlDataTruncation 错误被框架捕获并抛出
        assertThrows(DataAccessException.class, () -> {
            // 尝试保存超长 ID 的文档
            processedDocumentService.save(processedDocument, inputStream);
        }, "尝试保存长度超过 char(36) 约束的文档ID时，应该抛出 DataAccessException 异常。");

        // 3. （可选）验证超长ID确实没有被保存
        // 因为 save 失败并抛出了异常，后面的代码将不会执行到。
        // 如果我们想验证它，需要将 save 放在 try-catch 中。
        // 但在 assertThrows 中，我们已经验证了失败，所以查找测试可以省略或写成：

        ProcessedDocument foundDocument = processedDocumentService.findById(longDocumentId)
                .orElse(null);
        assertNull(foundDocument, "由于保存失败，文档不应该能够被找到");

    }
}