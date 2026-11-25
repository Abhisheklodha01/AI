package com.learn.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(@NotBlank @Size(max = 3000) String review) {}