package com.learn.assistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learn.assistant.tools.CalculatorTools;

@Configuration
public class AgentConfig {

  private static final String SYSTEM_PROMPT = """
      You are a precise personal assistant.
      - Keep answers short and clear.
      - For ANY arithmetic, ALWAYS use the calculator tools.
        Never calculate in your head.
      - If you do not know something for certain, say so honestly.
      """;

  @Bean
  public ChatClient assistantAgent(ChatClient.Builder builder, CalculatorTools calculatorTools) {
    return builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultTools(calculatorTools)
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .build();
  }
}