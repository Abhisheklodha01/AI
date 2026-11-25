package com.learn.knowledge.service;

import com.learn.knowledge.dto.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse upload(MultipartFile file);

    List<DocumentResponse> findAll();
}