package com.learn.knowledge.dto;

public record SourceDto(int number, String fileName, String documentId,
                        Integer chunkIndex, double score, String matchedBy, String preview) {}