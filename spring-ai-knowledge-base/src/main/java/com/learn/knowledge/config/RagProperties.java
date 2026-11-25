package com.learn.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(int topK,
                double similarityThreshold,
                String notFoundMessage,
                int chunkSize,
                boolean hybridEnabled,
                int candidatesPerSearch) {
}