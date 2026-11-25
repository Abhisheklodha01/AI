package com.learn.knowledge.controller;

import com.learn.knowledge.dto.CompareRequest;
import com.learn.knowledge.dto.CompareResponse;
import com.learn.knowledge.dto.EmbedRequest;
import com.learn.knowledge.dto.EmbedResponse;
import com.learn.knowledge.service.EmbeddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/embeddings")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @PostMapping
    public EmbedResponse embed(@Valid @RequestBody EmbedRequest request) {
        return embeddingService.embed(request.text());
    }

    @PostMapping("/compare")
    public CompareResponse compare(@Valid @RequestBody CompareRequest request) {
        return embeddingService.compare(request.textA(), request.textB());
    }
}