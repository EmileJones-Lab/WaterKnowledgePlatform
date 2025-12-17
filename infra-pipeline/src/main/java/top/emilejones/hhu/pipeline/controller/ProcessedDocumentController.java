package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.ProcessedDocumentRepository;
import top.emilejones.hhu.pipeline.services.ProcessedDocumentService;

import java.io.InputStream;
import java.util.Optional;


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

}
