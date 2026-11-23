package com.learn.assistant.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.core.Ordered;

public class TokenUsageAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageAdvisor.class);

    private final int order;

    public TokenUsageAdvisor(int order) {
        this.order = order;
    }

    public TokenUsageAdvisor() {
        this(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // ---- BEFORE ----
        Object conversationId = request.context().get(ChatMemory.CONVERSATION_ID);
        long start = System.currentTimeMillis();

        // ---- CALL THE REST OF THE CHAIN ----
        ChatClientResponse response = chain.nextCall(request);

        // ---- AFTER ----
        long elapsedMs = System.currentTimeMillis() - start;

        if (response.chatResponse() != null) {
            Usage usage = response.chatResponse().getMetadata().getUsage();
            log.info("conversation={} | time={} ms | prompt={} | completion={} | total={}",
                    conversationId, elapsedMs,
                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        }
        return response;
    }

    @Override
    public String getName() {
        return "TokenUsageAdvisor";
    }

    @Override
    public int getOrder() {
        return order;
    }
}