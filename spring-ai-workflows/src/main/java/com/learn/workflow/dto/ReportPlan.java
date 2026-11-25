package com.learn.workflow.dto;

import java.util.List;

public record ReportPlan(String title, List<SubTask> subTasks) {
}