package com.learn.knowledge.controller;

import com.learn.knowledge.dto.AnswerResponse;
import com.learn.knowledge.dto.QuestionRequest;
import com.learn.knowledge.dto.SearchResponse;
import com.learn.knowledge.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @PostMapping("/search")
    public SearchResponse search(@Valid @RequestBody QuestionRequest request) {
        return qaService.search(request.question());
    }

    @PostMapping("/ask")
    public AnswerResponse ask(@Valid @RequestBody QuestionRequest request) {
        return qaService.ask(request.question());
    }
}