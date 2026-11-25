package com.learn.workflow.service;

import com.learn.workflow.dto.ReviewAnalysisResponse;
import com.learn.workflow.dto.ReviewRequest;

public interface ReviewService {
    ReviewAnalysisResponse analyze(ReviewRequest request);
}