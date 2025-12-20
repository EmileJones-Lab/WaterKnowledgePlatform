package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.InputStream;
import java.util.Optional;


/**
 * Ocr处理后文档仓库实现，负责接入领域仓储接口并委派服务层。
 * 它实现了ProcessedDocumentRepository接口，通过调用ProcessedDocumentService来完成具体的业务处理。
 * @author Yeyezhi
 */
@Component
public class ProcessedDocumentController implements ProcessedDocumentRepository {

    private final ProcessedDocumentService processedDocumentService;

    public ProcessedDocumentController(ProcessedDocumentService processedDocumentService) {
        this.processedDocumentService = processedDocumentService;
    }

    @Override
    public void save(@NotNull ProcessedDocument processedDocument, @NotNull InputStream content) {
        processedDocumentService.save(processedDocument, content);
    }

    @Override
    public @NotNull Optional<ProcessedDocument> findById(@NotNull String id) {

        return processedDocumentService.findById(id);
    }

    @Override
    @NotNull
    public InputStream openContent(@NotNull String filePath) {
        return processedDocumentService.openContent(filePath);
    }

    @Override
    public void delete(@NotNull String markdownDocumentId) {
        processedDocumentService.delete(markdownDocumentId);
    }

    @Override
    public void deleteBySourceDocumentId(@NotNull String sourceDocumentId) {
        processedDocumentService.deleteBySourceDocumentId(sourceDocumentId);
    }

}
