package com.learn.knowledge.service;

import com.learn.knowledge.dto.AnswerResponse;
import com.learn.knowledge.dto.SearchResponse;

public interface QaService {

    /** Retrieval only: which chunks match the question? */
    SearchResponse search(String question);

    /** Full RAG: retrieve chunks, then let the LLM answer from them. */
    AnswerResponse ask(String question);
}