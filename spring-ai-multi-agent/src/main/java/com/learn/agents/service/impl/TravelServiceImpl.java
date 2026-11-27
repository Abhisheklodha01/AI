package com.learn.agents.service.impl;

import com.learn.agents.dto.TravelRequest;
import com.learn.agents.dto.TravelResponse;
import com.learn.agents.service.AgentCallTracker;
import com.learn.agents.service.TravelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelServiceImpl implements TravelService {

    @Qualifier("plannerAgent")
    private final ChatClient plannerAgent;

    @Override
    public TravelResponse plan(TravelRequest request) {
        long start = System.currentTimeMillis();
        AgentCallTracker tracker = new AgentCallTracker(); // new for every request

        String answer = plannerAgent.prompt()
                .user(request.message())
                .toolContext(Map.of(AgentCallTracker.KEY, tracker))
                .call()
                .content();

        long timeTaken = System.currentTimeMillis() - start;
        log.info("Planner finished in {} ms after consulting {} agent(s)", timeTaken, tracker.calls().size());
        return new TravelResponse(answer, tracker.calls(), timeTaken);
    }
}