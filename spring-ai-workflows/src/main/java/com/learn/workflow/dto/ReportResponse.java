package com.learn.workflow.dto;

import java.util.List;

public record ReportResponse(ReportPlan plan,
        List<SectionResult> sections,
        String finalReport,
        long timeTakenMs) {
}