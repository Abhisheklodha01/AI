package com.learn.assistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learn.assistant.advisor.TokenUsageAdvisor;
import com.learn.assistant.tools.CalculatorTools;
import com.learn.assistant.tools.DateTimeTools;

@Configuration
public class AgentConfig {

  private static final String SYSTEM_PROMPT = """
      You are a precise personal assistant.
      - Keep answers short and clear.
      - For ANY arithmetic, ALWAYS use the calculator tools.
        Never calculate in your head.
      - For ANY question about dates, days or time, ALWAYS use the date/time tools.
        Never assume today's date
      - If you do not know something for certain, say so honestly.
      """;

  @Bean
  public ChatClient assistantAgent(ChatClient.Builder builder,
      ChatMemory chatMemory,
      CalculatorTools calculatorTools,
      DateTimeTools dateTimeTools) {
    return builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultTools(calculatorTools, dateTimeTools)
        .defaultAdvisors(new TokenUsageAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
        .build();
  }

  @Bean
  public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(jdbcChatMemoryRepository)
        .maxMessages(20)
        .build();
  }
}