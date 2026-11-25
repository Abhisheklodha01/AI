package com.learn.workflow.dto;

public record ArticleResponse(ArticleBrief brief,
                              ArticleOutline outline,
                              String draft,
                              String finalArticle,
                              long timeTakenMs) {}