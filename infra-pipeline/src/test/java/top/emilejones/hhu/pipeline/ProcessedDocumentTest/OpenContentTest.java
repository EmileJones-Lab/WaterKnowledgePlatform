package top.emilejones.hhu.pipeline.ProcessedDocumentTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.ProcessedDocumentMapper;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 处理后文档打开内容流测试类
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class OpenContentTest {

    @Autowired
    private ProcessedDocumentService processedDocumentService;
    @Autowired
    private ProcessedDocumentMapper processedDocumentMapper;

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
        createdDocumentIds.forEach(id -> processedDocumentMapper.hardDelete(id));
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
     * 测试打开Markdown文档内容流
     */
    @Test
    public void openMarkdownContentTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-markdown.md";
        String filePath = testBaseDir + "/" + fileName;
        String content = "# Test Markdown Document\n\nThis is a test markdown file with multiple lines.\n\n## Section 1\nSome content here.\n\n## Section 2\nMore content here.";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "内容流不应该为null");

            // 读取内容并验证
            String readContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, readContent, "读取的内容应该与保存的内容一致");
        }
    }

    /**
     * 测试打开图片文档内容流
     */
    @Test
    public void openImageContentTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "test-image.png";
        String filePath = testBaseDir + "/" + fileName;

        // 创建模拟的PNG文件头和一些数据
        byte[] imageContent = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG文件头
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk开始
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1像素
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41,
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, 0x00, 0x02, 0x01, (byte) 0x82, (byte) 0xDD, (byte) 0x8D, (byte) 0xB4,
                (byte) 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
                0x60, (byte) 0x82 // IEND chunk
        };

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

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "图片内容流不应该为null");

            // 读取内容并验证
            byte[] readContent = contentStream.readAllBytes();
            assertArrayEquals(imageContent, readContent, "读取的二进制内容应该与保存的内容一致");
        }
    }

    /**
     * 测试打开空文件的内容流
     */
    @Test
    public void openEmptyFileContentTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "empty-file.txt";
        String filePath = testBaseDir + "/" + fileName;

        // 创建并保存空文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "空文件的内容流不应该为null");

            // 验证内容为空
            byte[] readContent = contentStream.readAllBytes();
            assertEquals(0, readContent.length, "空文件读取的内容长度应该为0");
        }
    }

    /**
     * 测试打开不存在的文件
     */
    @Test
    public void openNonExistingFileContentTest() {
        String nonExistingFilePath = testBaseDir + "/non-existing-file.txt";

        // 尝试打开不存在的文件，应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processedDocumentService.openContent(nonExistingFilePath);
        }, "打开不存在的文件应该抛出RuntimeException");

        assertTrue(exception.getMessage().contains("File not found"),
                "异常消息应该包含'File not found'");
    }

    /**
     * 测试打开包含特殊字符路径的文件
     */
    @Test
    public void openSpecialCharactersPathTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "file-with-special-chars_测试.txt";
        String filePath = testBaseDir + "/" + fileName;
        String content = "This file contains special characters in its name: " + fileName;

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "包含特殊字符路径的文件内容流不应该为null");

            String readContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, readContent, "特殊字符路径文件的内容应该正确读取");
        }
    }

    /**
     * 测试打开深层目录中的文件
     */
    @Test
    public void openNestedDirectoryFileTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String nestedPath = testBaseDir + "/level1/level2/level3/deep-nested-file.md";
        String content = "# Deep Nested File\n\nThis file is located in a deeply nested directory structure.";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                nestedPath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(nestedPath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(nestedPath)) {
            assertNotNull(contentStream, "深层目录中文件的内容流不应该为null");

            String readContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(content, readContent, "深层目录中文件的内容应该正确读取");
        }

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
     * 测试打开大文件的内容流
     */
    @Test
    public void openLargeFileContentTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "large-file.txt";
        String filePath = testBaseDir + "/" + fileName;

        // 创建较大的内容（约1MB）
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            largeContent.append("Line ").append(i).append(": This is a line of content for testing large file handling.\n");
        }
        String content = largeContent.toString();

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "大文件的内容流不应该为null");

            // 分块读取大文件内容并验证
            StringBuilder readContent = new StringBuilder();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = contentStream.read(buffer)) != -1) {
                readContent.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }

            assertEquals(content, readContent.toString(), "大文件的内容应该完整读取");
            assertTrue(readContent.length() > 1024 * 100, "读取的内容应该大于100KB");
        }
    }

    /**
     * 测试打开没有读取权限的文件
     */
    @Test
    public void openUnreadableFileTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "unreadable-file.txt";
        String filePath = testBaseDir + "/" + fileName;
        String content = "This file should be unreadable for testing purposes.";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 尝试移除文件的读取权限（在Windows上可能不会生效，但至少会测试代码路径）
        Path filePathObj = Paths.get(filePath);
        try {
            Files.setPosixFilePermissions(filePathObj, java.nio.file.attribute.PosixFilePermissions.fromString("---"));
        } catch (UnsupportedOperationException e) {
            // Windows系统不支持POSIX权限，跳过这个测试
            return;
        } catch (Exception e) {
            // 如果无法设置权限，也跳过测试
            return;
        }

        // 尝试打开没有读取权限的文件
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processedDocumentService.openContent(filePath);
        }, "打开没有读取权限的文件应该抛出RuntimeException");

        // 恢复权限以便清理
        try {
            Files.setPosixFilePermissions(filePathObj,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-r--r--"));
        } catch (Exception e) {
            // 忽略恢复权限的异常
        }

        assertTrue(exception.getMessage().contains("cannot be read"),
                "异常消息应该包含'cannot be read'");
    }

    /**
     * 测试打开内容流的参数验证
     */
    @Test
    public void openContentParameterValidationTest() {
        // 测试null路径
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.openContent(null);
        }, "null路径应该抛出IllegalArgumentException");

        // 测试空字符串路径
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.openContent("");
        }, "空字符串路径应该抛出IllegalArgumentException");

        // 测试空白字符串路径
        assertThrows(IllegalArgumentException.class, () -> {
            processedDocumentService.openContent("   ");
        }, "空白字符串路径应该抛出IllegalArgumentException");
    }

    /**
     * 测试多次打开同一文件的内容流
     */
    @Test
    public void openMultipleStreamsSameFileTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "multi-stream-test.txt";
        String filePath = testBaseDir + "/" + fileName;
        String content = "This file will be opened multiple times for testing.\nMultiple streams should be available.";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 同时打开多个内容流
        try (InputStream stream1 = processedDocumentService.openContent(filePath);
             InputStream stream2 = processedDocumentService.openContent(filePath);
             InputStream stream3 = processedDocumentService.openContent(filePath)) {

            assertNotNull(stream1, "第一个流不应该为null");
            assertNotNull(stream2, "第二个流不应该为null");
            assertNotNull(stream3, "第三个流不应该为null");

            // 从每个流读取内容并验证
            String content1 = new String(stream1.readAllBytes(), StandardCharsets.UTF_8);
            String content2 = new String(stream2.readAllBytes(), StandardCharsets.UTF_8);
            String content3 = new String(stream3.readAllBytes(), StandardCharsets.UTF_8);

            assertEquals(content, content1, "第一个流的内容应该正确");
            assertEquals(content, content2, "第二个流的内容应该正确");
            assertEquals(content, content3, "第三个流的内容应该正确");
        }
    }

    /**
     * 测试打开包含Unicode字符的文件内容
     */
    @Test
    public void openUnicodeContentTest() throws IOException {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String fileName = "unicode-test.txt";
        String filePath = testBaseDir + "/" + fileName;
        String unicodeContent = "Unicode Test File\n\nEnglish: Hello World\n" +
                "Chinese: 你好世界\n" +
                "Japanese: こんにちは世界\n" +
                "Korean: 안녕하세요 세계\n" +
                "Arabic: مرحبا بالعالم\n" +
                "Russian: Привет мир\n" +
                "Emoji: 🌍🌎🌏🌐\n" +
                "Special: àáâãäåæçèéêë";

        // 创建并保存文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(unicodeContent.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);
        createdDocumentIds.add(documentId);
        createdFilePaths.add(filePath);

        // 打开内容流
        try (InputStream contentStream = processedDocumentService.openContent(filePath)) {
            assertNotNull(contentStream, "Unicode文件的内容流不应该为null");

            String readContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(unicodeContent, readContent, "Unicode内容应该正确读取");
        }
    }
}