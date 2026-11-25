package com.learn.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequest(@NotBlank @Size(max = 1000) String prompt) {
}