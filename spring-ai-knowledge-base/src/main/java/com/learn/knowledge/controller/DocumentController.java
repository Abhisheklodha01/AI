package com.learn.knowledge.controller;

import com.learn.knowledge.dto.DocumentListResponse;
import com.learn.knowledge.dto.DocumentResponse;
import com.learn.knowledge.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@RequestParam("file") MultipartFile file) {
        return documentService.upload(file);
    }

    @GetMapping
    public DocumentListResponse list() {
        return new DocumentListResponse(documentService.findAll());
    }
}