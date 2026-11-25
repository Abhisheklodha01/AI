package com.learn.knowledge.dto;

import org.springframework.ai.document.Document;

/** One chunk found by retrieval, with how it was found. */
public record RetrievedChunk(Document document, double score, String matchedBy) {}