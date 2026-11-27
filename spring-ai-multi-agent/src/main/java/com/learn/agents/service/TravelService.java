package com.learn.agents.service;

import com.learn.agents.dto.TravelRequest;
import com.learn.agents.dto.TravelResponse;

public interface TravelService {
    TravelResponse plan(TravelRequest request);
}