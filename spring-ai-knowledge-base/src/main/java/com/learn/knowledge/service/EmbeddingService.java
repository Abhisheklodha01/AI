package com.learn.knowledge.service;

import com.learn.knowledge.dto.CompareResponse;
import com.learn.knowledge.dto.EmbedResponse;

public interface EmbeddingService {

    EmbedResponse embed(String text);

    CompareResponse compare(String textA, String textB);
}