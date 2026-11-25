package com.learn.workflow.controller;

import com.learn.workflow.dto.SupportRequest;
import com.learn.workflow.dto.SupportResponse;
import com.learn.workflow.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping
    public SupportResponse handle(@Valid @RequestBody SupportRequest request) {
        return supportService.handle(request);
    }
}