package com.learn.workflow.dto;

import com.learn.workflow.enums.SupportCategory;

public record RouteDecision(SupportCategory category, String reason) {
}