package com.learn.workflow.dto;

import com.learn.workflow.enums.Sentiment;

public record SentimentResult(Sentiment sentiment, int rating, String summary) {}