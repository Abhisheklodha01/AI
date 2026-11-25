package com.learn.workflow.controller;

import com.learn.workflow.dto.ReportRequest;
import com.learn.workflow.dto.ReportResponse;
import com.learn.workflow.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ReportResponse write(@Valid @RequestBody ReportRequest request) {
        return reportService.write(request);
    }
}