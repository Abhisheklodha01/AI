package com.learn.workflow.service;

import com.learn.workflow.dto.ArticleRequest;
import com.learn.workflow.dto.ArticleResponse;

public interface ArticleService {
    ArticleResponse write(ArticleRequest request);
}