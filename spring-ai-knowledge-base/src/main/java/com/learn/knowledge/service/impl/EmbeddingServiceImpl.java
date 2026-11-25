package com.learn.knowledge.service.impl;

import com.learn.knowledge.dto.CompareResponse;
import com.learn.knowledge.dto.EmbedResponse;
import com.learn.knowledge.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int PREVIEW_SIZE = 8;

    private final EmbeddingModel embeddingModel;

    @Override
    public EmbedResponse embed(String text) {
        float[] vector = embeddingModel.embed(text);
        log.info("Embedded text ({} chars) into {} dimensions", text.length(), vector.length);

        List<Float> preview = new ArrayList<>();
        for (int i = 0; i < Math.min(PREVIEW_SIZE, vector.length); i++) {
            preview.add(vector[i]);
        }
        return new EmbedResponse(text, vector.length, preview);
    }

    @Override
    public CompareResponse compare(String textA, String textB) {
        float[] a = embeddingModel.embed(textA);
        float[] b = embeddingModel.embed(textB);
        double similarity = cosineSimilarity(a, b);
        log.info("Similarity {} between '{}' and '{}'", similarity, textA, textB);
        return new CompareResponse(textA, textB, Math.round(similarity * 1000) / 1000.0);
    }

    /** Cosine similarity: 1 = same direction (same meaning), 0 = unrelated. */
    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}