package top.emilejones.hhu.pipeline.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import top.emilejones.hhu.domain.result.ProcessedDocument;
import top.emilejones.hhu.pipeline.constant.DeleteConstant;
import top.emilejones.hhu.pipeline.entity.ProcessedDocumentPo;
import top.emilejones.hhu.pipeline.mapper.ProcessedDocumentMapper;
import top.emilejones.hhu.pipeline.repository.FileStorageRepository;
import top.emilejones.hhu.pipeline.utils.PoToDomainUtil;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Ocr处理后文档服务实现类
 *
 * @author Yeyezhi
 */
@Service
public class ProcessedDocumentService {

    private final ProcessedDocumentMapper processedDocumentMapper;
    private final FileStorageRepository fileStorageRepository;

    public ProcessedDocumentService(ProcessedDocumentMapper processedDocumentMapper,
                                    FileStorageRepository fileStorageRepository) {
        this.processedDocumentMapper = processedDocumentMapper;
        this.fileStorageRepository = fileStorageRepository;
    }

    /**
     * 保存文档与其内容。
     * 约定：
     * - 实现幂等/覆盖语义，确保同一标识重复写入不会产生脏数据。
     * - 内容流的打开与关闭由调用方负责；实现只消费输入流并持久化，不应尝试重置或重复读取流。
     *
     * @param processedDocument 文档元数据（标识、标题等）
     * @param content           Markdown 正文内容流；调用方负责在写入完成后关闭流
     */
    public void save(@NotNull ProcessedDocument processedDocument, @NotNull InputStream content) {
        // 1. 存入 MinIO
        fileStorageRepository.save(content, processedDocument.getFilePath());

        // 2. 记录元数据
        ProcessedDocumentPo po = convertToPo(processedDocument);
        processedDocumentMapper.upsertProcessedDocument(po);
    }

    /**
     * 根据标识查询文档元数据。
     * 约定：未找到记录时返回 `Optional.empty()`；调用方需处理未命中分支。
     *
     * @param id 文档标识
     * @return 对应的文档元数据；未命中返回 Optional.empty
     */
    public Optional<ProcessedDocument> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Processed document ID cannot be blank");
        }

        ProcessedDocumentPo po = processedDocumentMapper.findById(id);
        return po != null ? Optional.of(PoToDomainUtil.toProcessedDocumentDomain(po)) : Optional.empty();
    }

    /**
     * 删除指定的处理后文档（软删除）。
     *
     * @param processedDocumentId 待删除处理后文档的ID。
     */
    public void delete(String processedDocumentId) {
        if (processedDocumentId == null || processedDocumentId.isBlank()) {
            throw new IllegalArgumentException("Processed document ID cannot be blank");
        }

        ProcessedDocumentPo po = new ProcessedDocumentPo();
        po.setProcessedDocumentId(processedDocumentId);
        po.setIsDelete(DeleteConstant.DELETE);

        processedDocumentMapper.softDelete(po);
    }

    /**
     * 根据源文档标识删除处理后文档（软删除）。
     *
     * @param sourceDocumentId 源文档的唯一标识
     */
    public void deleteBySourceDocumentId(String sourceDocumentId) {
        if (sourceDocumentId == null || sourceDocumentId.isBlank()) {
            throw new IllegalArgumentException("Source document ID cannot be blank");
        }

        processedDocumentMapper.softDeleteBySourceDocumentId(sourceDocumentId, DeleteConstant.DELETE);
    }

    /**
     * 打开文档内容流，用于上层读取内容。
     * 约定：
     * - 返回的流需由调用方关闭；未找到内容时应抛出可定位的异常。
     *
     * @param filePath 文档内容的存储路径或键
     * @return 文档内容流，调用方负责关闭
     */
    @NotNull
    public InputStream openContent(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be blank");
        }

        // 这里的 filePath 传入的是数据库里存的 MinIO 路径（如 /bamboo/2024/xxx.md）
        return fileStorageRepository.open(filePath);
    }

    /**
     * 封装领域对象 ProcessedDocument 到持久化对象 ProcessedDocumentPo 中
     */
    private ProcessedDocumentPo convertToPo(ProcessedDocument processedDocument) {
        ProcessedDocumentPo po = new ProcessedDocumentPo();
        po.setProcessedDocumentId(processedDocument.getId());
        po.setSourceDocumentId(processedDocument.getSourceDocumentId());
        po.setFilePath(processedDocument.getFilePath());
        po.setCreateTime(processedDocument.getCreateTime());
        po.setType(processedDocument.getProcessedDocumentType());
        // 设置删除标记为存在状态
        po.setIsDelete(DeleteConstant.EXIST);

        // 从文件路径中提取文件名
        String s = resolveObjectName(processedDocument.getFilePath());
        po.setFileName(s);

        return po;
    }

    public static String resolveObjectName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        String fileName = Paths.get(filePath).getFileName().toString();

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && fileName.substring(lastDotIndex).equalsIgnoreCase(".pdf")) {
            return fileName.substring(0, lastDotIndex) + ".md";
        }

        return fileName;
    }
}
