package com.learn.workflow.controller;

import com.learn.workflow.dto.ReviewAnalysisResponse;
import com.learn.workflow.dto.ReviewRequest;
import com.learn.workflow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/analyze")
    public ReviewAnalysisResponse analyze(@Valid @RequestBody ReviewRequest request) {
        return reviewService.analyze(request);
    }
}