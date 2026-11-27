package com.learn.agents.service;

import com.learn.agents.dto.AgentCall;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AgentCallTracker {

    public static final String KEY = "agentCallTracker";

    private final List<AgentCall> calls = new CopyOnWriteArrayList<>();

    public void record(String agent, String question, String answer) {
        calls.add(new AgentCall(agent, question, answer));
    }

    public List<AgentCall> calls() {
        return List.copyOf(calls);
    }
}