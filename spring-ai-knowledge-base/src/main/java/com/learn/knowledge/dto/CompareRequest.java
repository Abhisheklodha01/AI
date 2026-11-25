package com.learn.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompareRequest(
                @NotBlank @Size(max = 2000) String textA,
                @NotBlank @Size(max = 2000) String textB) {
}