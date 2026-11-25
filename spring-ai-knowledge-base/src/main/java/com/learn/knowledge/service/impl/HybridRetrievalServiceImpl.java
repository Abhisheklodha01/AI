package com.learn.knowledge.service.impl;

import com.learn.knowledge.config.RagProperties;
import com.learn.knowledge.dto.RetrievedChunk;
import com.learn.knowledge.repository.ChunkKeywordRepository;
import com.learn.knowledge.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridRetrievalServiceImpl implements RetrievalService {

    private static final int RRF_K = 60;

    private final VectorStore vectorStore;
    private final ChunkKeywordRepository keywordRepository;
    private final RagProperties ragProperties;

    @Override
    public List<RetrievedChunk> retrieve(String question) {
        List<Document> vectorResults = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(ragProperties.candidatesPerSearch())
                .similarityThreshold(ragProperties.similarityThreshold())
                .build());

        List<Document> keywordResults = ragProperties.hybridEnabled()
                ? keywordRepository.search(question, ragProperties.candidatesPerSearch())
                : List.of();

        log.info("Retrieval for '{}': vector={} keyword={}",
                question, vectorResults.size(), keywordResults.size());

        return fuse(vectorResults, keywordResults);
    }

    /** Reciprocal Rank Fusion: combine two ranked lists using positions only. */
    private List<RetrievedChunk> fuse(List<Document> vector, List<Document> keyword) {
        Map<String, Document> docs = new LinkedHashMap<>();
        Map<String, Double> scores = new HashMap<>();
        Map<String, Set<String>> foundBy = new HashMap<>();

        addRanked(vector, "VECTOR", docs, scores, foundBy);
        addRanked(keyword, "KEYWORD", docs, scores, foundBy);

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(ragProperties.topK())
                .map(e -> new RetrievedChunk(
                        docs.get(e.getKey()),
                        e.getValue(),
                        foundBy.get(e.getKey()).size() == 2 ? "BOTH" : foundBy.get(e.getKey()).iterator().next()))
                .toList();
    }

    private void addRanked(List<Document> results, String source,
                           Map<String, Document> docs, Map<String, Double> scores,
                           Map<String, Set<String>> foundBy) {
        for (int position = 0; position < results.size(); position++) {
            Document doc = results.get(position);
            docs.putIfAbsent(doc.getId(), doc);
            scores.merge(doc.getId(), 1.0 / (RRF_K + position + 1), Double::sum);
            foundBy.computeIfAbsent(doc.getId(), id -> new HashSet<>()).add(source);
        }
    }
}