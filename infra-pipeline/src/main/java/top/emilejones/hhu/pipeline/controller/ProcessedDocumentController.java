package top.emilejones.hhu.pipeline.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import top.emilejones.hhu.domain.pipeline.ProcessedDocument;
import top.emilejones.hhu.domain.pipeline.infrastructure.repository.ProcessedDocumentRepository;

import java.io.InputStream;
import java.util.Optional;


@Component
public class ProcessedDocumentController implements ProcessedDocumentRepository {

    @Override
    public void save(@NotNull ProcessedDocument processedDocument, @NotNull InputStream content) {

    }

    @Override
    public @NotNull Optional<ProcessedDocument> findById(@NotNull String id) {
        return Optional.empty();
    }

    @Override
    @NotNull
    public InputStream openContent(@NotNull String filePath) {
        return null;
    }
}
