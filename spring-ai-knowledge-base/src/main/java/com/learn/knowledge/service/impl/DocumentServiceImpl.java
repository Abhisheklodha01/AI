package com.learn.knowledge.service.impl;

import com.learn.knowledge.dto.DocumentResponse;
import com.learn.knowledge.entity.KnowledgeDocument;
import com.learn.knowledge.exception.NoTextExtractedException;
import com.learn.knowledge.exception.UnsupportedFileTypeException;
import com.learn.knowledge.repository.KnowledgeDocumentRepository;
import com.learn.knowledge.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    public static final String META_DOCUMENT_ID = "documentId";
    public static final String META_FILE_NAME = "fileName";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "txt", "md", "xlsx", "xls", "csv");

    private final VectorStore vectorStore;
    private final KnowledgeDocumentRepository documentRepository;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

    @Override
    public DocumentResponse upload(MultipartFile file) {
        String fileName = validate(file);
        UUID documentId = UUID.randomUUID();

        // 1. Read: file → text (Tika detects the format)
        List<Document> pages = new TikaDocumentReader(asResource(file, fileName)).read();

        // 2. Split: text → chunks
        List<Document> chunks = splitter.apply(pages).stream()
                .filter(chunk -> chunk.getText() != null && !chunk.getText().isBlank())
                .map(chunk -> withMetadata(chunk, documentId, fileName))
                .toList();

        if (chunks.isEmpty()) {
            throw new NoTextExtractedException(fileName);
        }

        // 3. Embed + store: each chunk → vector → vector_store table
        vectorStore.add(chunks);
        log.info("Stored {} chunks for '{}' ({})", chunks.size(), fileName, documentId);

        // 4. Record the document (only after the vectors are safely stored)
        KnowledgeDocument saved = documentRepository.save(KnowledgeDocument.builder()
                .id(documentId)
                .fileName(fileName)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .chunkCount(chunks.size())
                .build());

        return toResponse(saved);
    }

    @Override
    public List<DocumentResponse> findAll() {
        return documentRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- helpers ----------

    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String fileName = Optional.ofNullable(file.getOriginalFilename()).orElse("unnamed");
        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException(fileName);
        }
        return fileName;
    }

    private ByteArrayResource asResource(MultipartFile file, String fileName) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }
    }

    private Document withMetadata(Document chunk, UUID documentId, String fileName) {
        Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
        metadata.put(META_DOCUMENT_ID, documentId.toString());
        metadata.put(META_FILE_NAME, fileName);
        return new Document(chunk.getText(), metadata);
    }

    private DocumentResponse toResponse(KnowledgeDocument d) {
        return new DocumentResponse(d.getId(), d.getFileName(), d.getContentType(),
                d.getSizeBytes(), d.getChunkCount(), d.getUploadedAt());
    }
}