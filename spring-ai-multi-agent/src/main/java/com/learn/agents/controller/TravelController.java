package com.learn.agents.controller;

import com.learn.agents.dto.TravelRequest;
import com.learn.agents.dto.TravelResponse;
import com.learn.agents.service.TravelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @PostMapping("/plan")
    public TravelResponse plan(@Valid @RequestBody TravelRequest request) {
        return travelService.plan(request);
    }
}