package com.learn.agents.tools;

import com.learn.agents.service.AgentCallTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpecialistTools {

    @Qualifier("weatherAgent")
    private final ChatClient weatherAgent;

    @Qualifier("budgetAgent")
    private final ChatClient budgetAgent;

    @Qualifier("itineraryAgent")
    private final ChatClient itineraryAgent;

    @Tool(description = """
            Ask the weather expert about the typical weather and best season
            for a place in a given month.""")
    public String askWeatherExpert(
            @ToolParam(description = "A complete question including the place and month") String question,
            ToolContext toolContext) {
        return ask("weather", weatherAgent, question, toolContext);
    }

    @Tool(description = """
            Ask the budget expert to estimate trip costs in rupees.
            Include the destination, number of days, season and travel style.""")
    public String askBudgetExpert(
            @ToolParam(description = "A complete question with destination, days, season and style") String question,
            ToolContext toolContext) {
        return ask("budget", budgetAgent, question, toolContext);
    }

    @Tool(description = """
            Ask the itinerary expert to create a day-by-day plan.
            Include the destination, number of days and interests.""")
    public String askItineraryExpert(
            @ToolParam(description = "A complete question with destination, days and interests") String question,
            ToolContext toolContext) {
        return ask("itinerary", itineraryAgent, question, toolContext);
    }

    private String ask(String agentName, ChatClient agent, String question, ToolContext toolContext) {
        log.info("Planner → {} agent: {}", agentName, question);
        String answer = agent.prompt().user(question).call().content();
        log.info("{} agent replied ({} chars)", agentName, answer == null ? 0 : answer.length());

        if (toolContext.getContext().get(AgentCallTracker.KEY) instanceof AgentCallTracker tracker) {
            tracker.record(agentName, question, answer);
        }
        return answer;
    }
}