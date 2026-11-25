package com.learn.workflow.service;

import com.learn.workflow.dto.ReportRequest;
import com.learn.workflow.dto.ReportResponse;

public interface ReportService {
    ReportResponse write(ReportRequest request);
}