package com.learn.agents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TravelRequest(@NotBlank @Size(max = 1000) String message) {}