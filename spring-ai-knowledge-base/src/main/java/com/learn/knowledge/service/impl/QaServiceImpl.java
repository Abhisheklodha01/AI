package com.learn.knowledge.service.impl;

import com.learn.knowledge.config.RagProperties;
import com.learn.knowledge.dto.AnswerResponse;
import com.learn.knowledge.dto.RetrievedChunk;
import com.learn.knowledge.dto.SearchResponse;
import com.learn.knowledge.dto.SourceDto;
import com.learn.knowledge.service.QaService;
import com.learn.knowledge.service.RetrievalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private static final String USER_TEMPLATE = """
            CONTEXT:
            {context}

            QUESTION:
            {question}
            """;

    private final RetrievalService retrievalService;
    private final RagProperties ragProperties;

    @Qualifier("knowledgeAgent")
    private final ChatClient knowledgeAgent;

    @Override
    public SearchResponse search(String question) {
        return new SearchResponse(question, toSources(retrievalService.retrieve(question)));
    }

    @Override
    public AnswerResponse ask(String question) {
        List<RetrievedChunk> chunks = retrievalService.retrieve(question);

        if (chunks.isEmpty()) {
            log.info("No relevant chunks for '{}'", question);
            return new AnswerResponse(question, ragProperties.notFoundMessage(), List.of());
        }

        String answer = knowledgeAgent.prompt()
                .user(u -> u.text(USER_TEMPLATE)
                        .param("context", buildContext(chunks))
                        .param("question", question))
                .call()
                .content();

        return new AnswerResponse(question, answer, toSources(chunks));
    }

    private String buildContext(List<RetrievedChunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            Document doc = chunks.get(i).document();
            sb.append("[").append(i + 1).append("] (from ")
                    .append(doc.getMetadata().get(DocumentServiceImpl.META_FILE_NAME)).append(")\n")
                    .append(doc.getText()).append("\n\n");
        }
        return sb.toString();
    }

    private List<SourceDto> toSources(List<RetrievedChunk> chunks) {
        List<SourceDto> sources = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            Document doc = chunk.document();
            Object chunkIndex = doc.getMetadata().get("chunk_index");
            String text = doc.getText();
            sources.add(new SourceDto(
                    i + 1,
                    String.valueOf(doc.getMetadata().get(DocumentServiceImpl.META_FILE_NAME)),
                    String.valueOf(doc.getMetadata().get(DocumentServiceImpl.META_DOCUMENT_ID)),
                    chunkIndex == null ? null : Integer.valueOf(chunkIndex.toString()),
                    Math.round(chunk.score() * 10000) / 10000.0,
                    chunk.matchedBy(),
                    text.length() > 120 ? text.substring(0, 120) + "..." : text));
        }
        return sources;
    }
}