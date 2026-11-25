package com.learn.knowledge.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, String fileName, String contentType,
                               long sizeBytes, int chunkCount, Instant uploadedAt) {}