package com.learn.workflow.controller;

import com.learn.workflow.dto.ArticleRequest;
import com.learn.workflow.dto.ArticleResponse;
import com.learn.workflow.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public ArticleResponse write(@Valid @RequestBody ArticleRequest request) {
        return articleService.write(request);
    }
}