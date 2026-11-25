package com.learn.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportRequest(@NotBlank @Size(max = 2000) String message) {
}