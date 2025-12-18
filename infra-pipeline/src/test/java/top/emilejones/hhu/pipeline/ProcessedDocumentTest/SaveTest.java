package top.emilejones.hhu.pipeline.ProcessedDocumentTest;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;
import top.emilejones.hhu.pipeline.TestApplication;
import top.emilejones.hhu.pipeline.mapper.ProcessedDocumentMapper;
import top.emilejones.hhu.pipeline.repository.FileStorageRepository;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 处理后文档保存测试类
 * 基于Minio存储系统
 * @author Yeyezhi
 */
@SpringBootTest(classes = TestApplication.class)
public class SaveTest {

    @Autowired
    private ProcessedDocumentService processedDocumentService;
    @Autowired
    private ProcessedDocumentMapper processedDocumentMapper;
    @Autowired
    private FileStorageRepository fileStorageRepository;
    @Autowired
    private MinioClient minioClient;

    private List<String> createdDocumentIds = new ArrayList<>();
    private List<String> createdObjectNames = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createdDocumentIds.clear();
        createdObjectNames.clear();
    }

    @AfterEach
    void tearDown() {
        // 1. 清理数据库元数据
        if (!createdDocumentIds.isEmpty()) {
            createdDocumentIds.forEach(id -> {
                try {
                    processedDocumentMapper.hardDelete(id);
                } catch (Exception e) {
                    System.err.println("清理数据库失败，ID: " + id + ", 原因: " + e.getMessage());
                }
            });
            createdDocumentIds.clear();
        }

        // 2. 真正清理 Minio 中的物理文件
        if (!createdObjectNames.isEmpty()) {
            for (String path : createdObjectNames) {
                try {
                    // 解析路径 (格式如: /bamboo/uuid.md)
                    String cleanPath = path.startsWith("/") ? path.substring(1) : path;
                    int slashIndex = cleanPath.indexOf("/");
                    if (slashIndex > 0) {
                        String bucket = cleanPath.substring(0, slashIndex);
                        String objectKey = cleanPath.substring(slashIndex + 1);

                        // 执行 Minio 删除操作
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(objectKey)
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("清理 Minio 文件失败，路径: " + path + ", 原因: " + e.getMessage());
                }
            }
            createdObjectNames.clear();
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
        String filePath = "/tmp/test/" + fileName; // 原始路径，仅用于记录
        String content = "# Test Markdown Document\n\nThis is a test markdown file content.";

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 预期的Minio路径
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument, "保存的文档应该能够被找到");
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(expectedMinioPath, savedDocument.getFilePath());
        assertEquals(ProcessedDocumentType.MARKDOWN, savedDocument.getProcessedDocumentType());

        // 验证文件内容被正确保存到Minio
        try {
            String savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertEquals(content, savedContent, "Minio中的文件内容应该与输入内容一致");
        } catch (IOException e) {
            fail("读取Minio中的文件失败: " + e.getMessage());
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
        String filePath = "/tmp/test/" + fileName; // 原始路径，仅用于记录

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

        // 预期的Minio路径
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageContent);
        processedDocumentService.save(processedDocument, inputStream);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument, "保存的图片文档应该能够被找到");
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(expectedMinioPath, savedDocument.getFilePath());
        assertEquals(ProcessedDocumentType.PNG, savedDocument.getProcessedDocumentType());

        // 验证文件被正确保存到Minio
        try {
            byte[] savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = contentStream.readAllBytes();
            }
            assertArrayEquals(imageContent, savedContent, "Minio中的文件内容应该与输入内容一致");
        } catch (IOException e) {
            fail("读取Minio中的图片文件失败: " + e.getMessage());
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
        String filePath = "/tmp/test/" + fileName; // 原始路径，仅用于记录
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

        // 预期的Minio路径
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        ByteArrayInputStream originalInputStream = new ByteArrayInputStream(originalContent.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, originalInputStream);

        // 验证初始内容
        try {
            String savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertEquals(originalContent, savedContent);
        } catch (IOException e) {
            fail("读取Minio中的初始文件失败: " + e.getMessage());
        }

        // 更新文档内容
        ByteArrayInputStream updatedInputStream = new ByteArrayInputStream(updatedContent.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, updatedInputStream);

        // 验证文档元数据没有变化
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());
        assertEquals(sourceDocumentId, savedDocument.getSourceDocumentId());
        assertEquals(expectedMinioPath, savedDocument.getFilePath());

        // 验证Minio中的文件内容被更新
        try {
            String savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertEquals(updatedContent, savedContent, "Minio中的文件内容应该被更新为新内容");
        } catch (IOException e) {
            fail("读取Minio中更新后的文件失败: " + e.getMessage());
        }
    }

    /**
     * 测试保存到深层目录 (在Minio中路径平铺，没有深层目录结构)
     */
    @Test
    public void saveToNestedDirectoryTest() {
        String documentId = UUID.randomUUID().toString();
        String sourceDocumentId = UUID.randomUUID().toString();
        String nestedPath = "/tmp/nested/directory/deep/test.md"; // 原始路径，仅用于记录
        String content = "# Nested Directory Test\n\nThis file is saved in a nested directory.";

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                nestedPath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 预期的Minio路径（平铺结构）
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        // 保存文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);

        // 验证文档被正确创建
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证文件内容被正确保存到Minio
        try {
            String savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
            }
            assertEquals(content, savedContent, "Minio中的文件内容应该正确");
        } catch (IOException e) {
            fail("读取Minio中的文件失败: " + e.getMessage());
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
        String filePath = "/tmp/test/" + fileName; // 原始路径，仅用于记录
        byte[] emptyContent = new byte[0];

        // 创建处理后文档
        ProcessedDocument processedDocument = new ProcessedDocument(
                documentId,
                sourceDocumentId,
                filePath,
                Instant.now(),
                ProcessedDocumentType.MARKDOWN
        );

        // 预期的Minio路径
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        // 保存空内容文档
        ByteArrayInputStream inputStream = new ByteArrayInputStream(emptyContent);
        processedDocumentService.save(processedDocument, inputStream);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证空文件被正确保存到Minio
        try {
            byte[] savedContent;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContent = contentStream.readAllBytes();
            }
            assertEquals(0, savedContent.length, "Minio中的空文件大小应该为0");
        } catch (IOException e) {
            fail("检查Minio中的空文件失败: " + e.getMessage());
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
        String filePath = "/tmp/test/" + fileName; // 原始路径，仅用于记录

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

        // 预期的Minio路径
        String expectedMinioPath = "/bamboo/" + documentId + ".md";

        // 提前记录到清理列表中，确保即使save失败也能正确清理
        createdDocumentIds.add(documentId);
        createdObjectNames.add(expectedMinioPath);

        // 保存大文件
        ByteArrayInputStream inputStream = new ByteArrayInputStream(largeContent.toString().getBytes(StandardCharsets.UTF_8));
        processedDocumentService.save(processedDocument, inputStream);

        // 验证文档能够被成功保存
        ProcessedDocument savedDocument = processedDocumentService.findById(documentId)
                .orElse(null);
        assertNotNull(savedDocument);
        assertEquals(documentId, savedDocument.getId());

        // 验证大文件被正确保存到Minio
        try {
            byte[] savedContentBytes;
            try (InputStream contentStream = fileStorageRepository.open(expectedMinioPath)) {
                savedContentBytes = contentStream.readAllBytes();
            }
            assertTrue(savedContentBytes.length > 1024 * 100, "Minio中的文件大小应该大于100KB");

            String savedContent = new String(savedContentBytes, StandardCharsets.UTF_8);
            assertEquals(largeContent.toString(), savedContent, "Minio中的大文件内容应该完整保存");
        } catch (IOException e) {
            fail("读取Minio中的大文件失败: " + e.getMessage());
        }
    }
}