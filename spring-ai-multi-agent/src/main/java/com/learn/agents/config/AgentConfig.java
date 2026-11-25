package com.learn.agents.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AgentConfig {

    @Value("classpath:prompts/weather-agent.st")
    private Resource weatherPrompt;

    @Value("classpath:prompts/budget-agent.st")
    private Resource budgetPrompt;

    @Value("classpath:prompts/itinerary-agent.st")
    private Resource itineraryPrompt;

    @Bean
    public ChatClient weatherAgent(ChatClient.Builder builder) {
        return builder.defaultSystem(weatherPrompt).build();
    }

    @Bean
    public ChatClient budgetAgent(ChatClient.Builder builder) {
        return builder.defaultSystem(budgetPrompt).build();
    }

    @Bean
    public ChatClient itineraryAgent(ChatClient.Builder builder) {
        return builder.defaultSystem(itineraryPrompt).build();
    }
}