package com.learn.knowledge.dto;

import java.util.List;

public record AnswerResponse(String question, String answer, List<SourceDto> sources) {}