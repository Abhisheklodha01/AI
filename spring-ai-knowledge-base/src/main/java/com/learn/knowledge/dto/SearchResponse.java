package com.learn.knowledge.dto;

import java.util.List;

public record SearchResponse(String question, List<SourceDto> results) {}