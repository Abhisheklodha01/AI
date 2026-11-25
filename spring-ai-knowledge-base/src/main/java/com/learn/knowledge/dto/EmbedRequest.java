package com.learn.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmbedRequest(@NotBlank @Size(max = 2000) String text) {
}