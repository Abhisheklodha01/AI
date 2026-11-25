package com.learn.knowledge.dto;

import java.util.List;

public record EmbedResponse(String text, int dimensions, List<Float> preview) {
}