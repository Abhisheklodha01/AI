package com.learn.knowledge.service;

import com.learn.knowledge.dto.RetrievedChunk;

import java.util.List;

public interface RetrievalService {

    /** Find the chunks most relevant to the question. */
    List<RetrievedChunk> retrieve(String question);
}