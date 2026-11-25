package com.learn.workflow.dto;

import java.util.List;

public record ReviewAnalysisResponse(SentimentResult sentiment,
                                     List<String> issues,
                                     String suggestedReply,
                                     long parallelTimeMs,
                                     long sequentialTimeMs) {}