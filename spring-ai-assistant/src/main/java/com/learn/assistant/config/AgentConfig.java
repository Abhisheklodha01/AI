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
import com.learn.assistant.tools.ExpenseTools;

@Configuration
public class AgentConfig {

  private static final String SYSTEM_PROMPT = """
      You are a precise personal finance assistant.
      - Keep answers short and clear. Amounts are in Indian rupees (₹).
      - For ANY arithmetic, ALWAYS use the calculator tools.
      - For ANY question about dates, ALWAYS use the date/time tools first.
        Convert words like "this month" or "last week" into exact dates.
      - For questions about the user's spending, ALWAYS use the expense tools.
        Never invent or estimate expense data.
      - The user is already identified. Never ask for a user ID or name.
      - When a question can be interpreted in more than one way,
        state the assumption you made.
      """;
  private static final String EXPENSE_PROMPT = """
      You extract expenses from the user's text.
      - Today's date is {today}.
      - Convert relative dates ("today", "yesterday", "last Monday") into real dates.
      - If a date has no year, use the current year, unless that would put
        the date in the future; then use the previous year.
      - Amounts are in Indian rupees.
      - Pick the closest category. Use OTHER if nothing fits.
      - Never say an expense is saved unless the addExpense tool succeeded.
      - Only extract what the user actually said. Never invent expenses.
      """;

  @Bean
  public ChatClient expenseParser(ChatClient.Builder builder) {
    return builder
        .defaultSystem(EXPENSE_PROMPT)
        .defaultAdvisors(new TokenUsageAdvisor())
        .build();
  }

  @Bean
  public ChatClient assistantAgent(ChatClient.Builder builder,
      ChatMemory chatMemory,
      CalculatorTools calculatorTools,
      DateTimeTools dateTimeTools,
      ExpenseTools expenseTools) {
    return builder
        .defaultSystem(SYSTEM_PROMPT)
        .defaultTools(calculatorTools, dateTimeTools, expenseTools)
        .defaultAdvisors(new TokenUsageAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build(),
            new SimpleLoggerAdvisor())
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