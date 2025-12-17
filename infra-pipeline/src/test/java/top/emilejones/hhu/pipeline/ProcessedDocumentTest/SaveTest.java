package top.emilejones.hhu.pipeline.ProcessedDocumentTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 处理后文档保存测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {

    @Autowired
    private ProcessedDocumentService processedDocumentService;

    private final List<String> createdDocumentIds = new ArrayList<>();
    private final List<String> createdFilePaths = new ArrayList<>();
    private final String testBaseDir = "test-docs";

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
        for (String documentId : createdDocumentIds) {
            try {
                // 从数据库中删除（如果实现了删除方法）
                // processedDocumentService.delete(documentId);
            } catch (Exception e) {
                // 忽略删除异常
            }
        }
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
     * 测试保存Markdown类型文档
     */
    @Test
    public void saveMarkdownDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-document.md";
        String filePath = testBaseDir + "/" + fileName;
        String content = "# Test Markdown Document\n\nThis is a test markdown file content.";

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument, "保存的文档应该能够被找到");
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(filePath, savedDocument.getFilePath());
        assertEquals(ProcessedDocumentType.MARKDOWN, savedDocument.getProcessedDocumentType());

        // 验证文件内容被正确保存
        assertTrue(Files.exists(Paths.get(filePath)), "文件应该存在于文件系统中");
        try {
            String savedContent = Files.readString(Paths.get(filePath));
            assertEquals(content, savedContent, "文件内容应该与输入内容一致");
        } catch (IOException e) {
            fail("读取保存的文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试保存IMAGE类型文档
     */
    @Test
    public void saveImageDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-image.png";
        String filePath = testBaseDir + "/" + fileName;

        // 创建模拟的图片内容（实际上是一个简单的二进制数据）
        byte[] imageContent = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // PNG文件头

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.PNG
        );

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageContent);
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument, "保存的图片文档应该能够被找到");
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(filePath, savedDocument.getFilePath());
        assertEquals(ProcessedDocumentType.PNG, savedDocument.getProcessedDocumentType());

        // 验证文件被正确保存
        assertTrue(Files.exists(Paths.get(filePath)), "图片文件应该存在于文件系统中");
        try {
            byte[] savedContent = Files.readAllBytes(Paths.get(filePath));
            assertArrayEquals(imageContent, savedContent, "文件内容应该与输入内容一致");
        } catch (IOException e) {
            fail("读取保存的图片文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试更新已存在的文档（幂等性测试）
     */
    @Test
    public void updateExistingDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-update.md";
        String filePath = testBaseDir + "/" + fileName;
        String originalContent = "# Original Content\n\nThis is the original content.";
        String updatedContent = "# Updated Content\n\nThis is the updated content after modification.";

        // 创建并保存初始文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream originalInputStream = new ByteArrayInputStream(originalContent.getBytes());
        processedDocumentService.save(processedDocument, originalInputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 验证初始内容
        try {
            String savedContent = Files.readString(Paths.get(filePath));
            assertEquals(originalContent, savedContent);
        } catch (IOException e) {
            fail("读取初始文件失败: " + e.getMessage());
        }

        // 更新文档内容
        ByteArrayInputStream updatedInputStream = new ByteArrayInputStream(updatedContent.getBytes());
        processedDocumentService.save(processedDocument, updatedInputStream);

        // 验证文档元数据没有变化
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(filePath, savedDocument.getFilePath());

        // 验证文件内容被更新
        try {
            String savedContent = Files.readString(Paths.get(filePath));
            assertEquals(updatedContent, savedContent, "文件内容应该被更新为新内容");
        } catch (IOException e) {
            fail("读取更新后的文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试保存到深层目录
     */
    @Test
    public void saveToNestedDirectoryTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String nestedPath = testBaseDir + "/nested/directory/deep/test.md";
        String content = "# Nested Directory Test\n\nThis file is saved in a nested directory.";

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                nestedPath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(nestedPath);

        // 验证文档和嵌套目录都被正确创建
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证文件存在于嵌套目录中
        Path filePath = Paths.get(nestedPath);
        assertTrue(Files.exists(filePath), "文件应该存在于嵌套目录中");
        assertTrue(Files.isDirectory(filePath.getParent()), "父目录应该存在");

        // 清理嵌套目录
        try {
            Files.walk(Paths.get(testBaseDir))
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // 忽略删除异常
                        }
                    });
        } catch (IOException e) {
            // 忽略删除异常
        }
    }

    /**
     * 测试空内容的文档保存
     */
    @Test
    public void saveEmptyContentDocumentTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "empty-document.md";
        String filePath = testBaseDir + "/" + fileName;
        byte[] emptyContent = new byte[0];

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 保存空内容文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(emptyContent);
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证空文件被正确创建
        assertTrue(Files.exists(Paths.get(filePath)), "空文件应该存在于文件系统中");
        try {
            long fileSize = Files.size(Paths.get(filePath));
            assertEquals(0, fileSize, "空文件大小应该为0");
        } catch (IOException e) {
            fail("检查空文件大小失败: " + e.getMessage());
        }
    }

    /**
     * 测试大文件的保存
     */
    @Test
    public void saveLargeFileTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "large-document.md";
        String filePath = testBaseDir + "/" + fileName;

        // 创建较大的内容（约1MB）
        StringBuilder largeContent = new StringBuilder();
        largeContent.append("# Large Document\n\n");
        for (int i = 0; i < 10000; i++) {
            largeContent.append("This is line ").append(i)
                    .append(" of the large document with some content to make it bigger.\n");
        }

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 保存大文件
        ByteArrayInputStream inputStream = new ByteArrayInputStream(largeContent.toString().getBytes());
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证大文件被正确保存
        assertTrue(Files.exists(Paths.get(filePath)), "大文件应该存在于文件系统中");
        try {
            long fileSize = Files.size(Paths.get(filePath));
            assertTrue(fileSize > 1024 * 100, "文件大小应该大于100KB");

            String savedContent = Files.readString(Paths.get(filePath));
            assertEquals(largeContent.toString(), savedContent, "大文件内容应该完整保存");
        } catch (IOException e) {
            fail("读取大文件失败: " + e.getMessage());
        }
    }
}