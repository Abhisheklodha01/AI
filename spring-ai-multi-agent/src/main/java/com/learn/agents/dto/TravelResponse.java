package com.learn.agents.dto;

import java.util.List;

public record TravelResponse(String answer,
        List<AgentCall> agentsConsulted,
        long timeTakenMs) {
}