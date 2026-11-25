package com.learn.knowledge.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class AiConfig {

  @Value("classpath:prompts/knowledge-system.st")
  private Resource knowledgeSystemPrompt;

  @Bean
  public ChatClient knowledgeAgent(ChatClient.Builder builder, RagProperties ragProperties) {
    return builder
        .defaultSystem(s -> s.text(knowledgeSystemPrompt)
            .param("notFoundMessage", ragProperties.notFoundMessage()))
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .build();
  }
}